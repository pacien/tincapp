# Tinc Mesh VPN: Android client and user interface
# Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

"""
Emulated Android device running the app, driven through adb.

Requires on PATH: adb, avdmanager, emulator (with ANDROID_HOME and JAVA_HOME set), and the system image to use in
TINCAPP_TEST_SYSTEM_IMAGE.
"""

import contextlib
import io
import os
import random
import shlex
import signal
import socket
import subprocess
import tarfile
import time
from pathlib import Path

APP_PACKAGE = "org.pacien.tincapp"
SYSTEM_IMAGE = os.environ["TINCAPP_TEST_SYSTEM_IMAGE"]


def run(*args, text=True, timeout=120, **kwargs):
    try:
        result = subprocess.run(args, text=text, capture_output=True, timeout=timeout, **kwargs)
    except subprocess.TimeoutExpired:
        raise RuntimeError(f"{' '.join(args)} timed out after {timeout}s")
    if result.returncode != 0:
        output = [o if text else o.decode(errors="replace") for o in (result.stdout, result.stderr)]
        raise RuntimeError(f"{' '.join(args)} failed ({result.returncode}):\n" + "\n".join(output).strip())
    return result.stdout


def wait_until(description, predicate, timeout, interval=1.0):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if predicate():
            return
        time.sleep(interval)
    raise TimeoutError(f"timed out after {timeout}s waiting for {description}")


def is_port_free(port):
    with socket.socket() as s:
        try:
            s.bind(("127.0.0.1", port))
            return True
        except OSError:
            return False


def free_emulator_port():
    """Even console port in the range scanned by adb, with the next (adb) port also free."""
    candidates = [port for port in range(5554, 5586, 2) if is_port_free(port) and is_port_free(port + 1)]
    if not candidates:
        raise RuntimeError("no free emulator port")
    return random.choice(candidates)


class Device:
    def __init__(self, serial):
        self.serial = serial

    def adb(self, *args, **kwargs):
        return run("adb", "-s", self.serial, *args, **kwargs)

    def shell(self, command, **kwargs):
        return self.adb("shell", command, **kwargs)

    def run_as_app(self, command, **kwargs):
        return self.shell(f"run-as {APP_PACKAGE} sh -c '{command}'", **kwargs)

    def prop(self, name):
        try:
            return self.shell(f"getprop {name}", timeout=10).strip()
        except RuntimeError:
            return ""

    def install_app(self, apk):
        self.adb("install", "-r", "-t", str(apk))
        self.shell(f"appops set {APP_PACKAGE} ACTIVATE_VPN allow")

    def install_network(self, net_dir: Path):
        # Writable, as files from the Nix store are read-only.
        archive = subprocess.run(["tar", "-c", "--mode=u+w", "-C", str(net_dir.parent), net_dir.name],
                                 check=True, capture_output=True).stdout
        self.run_as_app("mkdir -p files/networks && tar -x -C files/networks", input=archive, text=False)
        if "tinc.conf" not in self.run_as_app(f"ls files/networks/{net_dir.name}").split():
            raise RuntimeError(f"network configuration {net_dir.name} not installed")

    def connect(self, net_name, address):
        """Connects through the app's Intent API, and waits for the VPN interface."""
        self.shell(f"am start -W -a {APP_PACKAGE}.intent.action.CONNECT -d tinc:{net_name}")
        wait_until("VPN interface", lambda: f"inet {address}/" in self.shell("ip -4 addr"), 60, 2)

    def ping(self, address, count=3):
        return self.shell(f"ping -c {count} -W 5 {address}")

    def instrument(self, test_package, arguments=None, runner="androidx.test.runner.AndroidJUnitRunner"):
        """Runs instrumented tests, returning whether they all passed, and their report."""
        options = "".join(f" -e {key} {shlex.quote(value)}" for key, value in (arguments or {}).items())
        report = self.shell(f"am instrument -w{options} {test_package}/{runner}", timeout=900)
        return "\nOK (" in report and "FAILURES!!!" not in report, report

    @contextlib.contextmanager
    def recording(self, destination: Path, time_limit=180):
        """Records the screen while in the context (up to the time limit), into the destination video file."""
        remote = "/data/local/tmp/tincapp-test-recording.mp4"
        recorder = subprocess.Popen(["adb", "-s", self.serial, "shell", f"screenrecord --time-limit {time_limit} {remote}"])
        try:
            yield
        finally:
            subprocess.run(["adb", "-s", self.serial, "shell", "pkill -INT screenrecord"], capture_output=True)
            recorder.wait(30)
            time.sleep(1)  # let the recorder finalise the file
            self.adb("pull", remote, str(destination))

    def pull_screenshots(self, destination: Path):
        """Copies the screenshots taken by the instrumented tests."""
        archive = self.run_as_app("tar -c -C cache test-screenshots", text=False)
        with tarfile.open(fileobj=io.BytesIO(archive)) as tar:
            tar.extractall(destination, filter="data")

    def save_diagnostics(self, out_dir: Path):
        for name, command in [("app.log", lambda: self.run_as_app("cat cache/logs/*")),
                              ("crash.txt", lambda: self.adb("logcat", "-d", "-b", "crash")),
                              ("logcat.txt", lambda: self.adb("logcat", "-d"))]:
            try:
                (out_dir / name).write_text(command())
            except RuntimeError as e:
                (out_dir / name).write_text(str(e))


class Emulator:
    """Fresh emulated device, in its own AVD and adb server. Only stops what it started."""

    def __init__(self, work_dir: Path):
        self.work_dir = work_dir
        self.process = None
        self.log = None

    def start(self) -> Device:
        os.environ["ANDROID_USER_HOME"] = str(self.work_dir / "android-user-home")
        os.environ["ANDROID_AVD_HOME"] = str(self.work_dir / "avd")
        os.environ.setdefault("ANDROID_ADB_SERVER_PORT", str(self._free_port()))
        os.makedirs(os.environ["ANDROID_AVD_HOME"], exist_ok=True)
        avd_name = f"tincapp-test-{os.urandom(4).hex()}"
        run("avdmanager", "create", "avd", "--name", avd_name, "--package", SYSTEM_IMAGE, input="no\n")

        console_port = free_emulator_port()
        self.log = open(self.work_dir / "emulator.log", "w")
        # 4 GB of memory: with less, the system kills the app during the first boot's background setup.
        self.process = subprocess.Popen(
            ["emulator", "-avd", avd_name, "-port", str(console_port), "-memory", "4096", "-cores", "4",
             "-no-window", "-no-audio", "-no-boot-anim", "-no-snapshot", "-gpu", "swiftshader_indirect"],
            stdout=self.log, stderr=subprocess.STDOUT, start_new_session=True)

        device = Device(f"emulator-{console_port}")

        def booted():
            if self.process.poll() is not None:
                raise RuntimeError(f"emulator exited with status {self.process.returncode}")
            return device.prop("sys.boot_completed") == "1"

        wait_until("boot completion", booted, 300, 3)
        # Guard against driving another emulator which would have taken the port meanwhile.
        if device.adb("emu", "avd", "name").split()[0] != avd_name:
            raise RuntimeError(f"{device.serial} is not the test emulator")
        device.adb("logcat", "-G", "16M")
        return device

    def stop(self):
        if self.process is not None and self.process.poll() is None:
            os.killpg(self.process.pid, signal.SIGTERM)
            try:
                self.process.wait(30)
            except subprocess.TimeoutExpired:
                os.killpg(self.process.pid, signal.SIGKILL)
                self.process.wait()
        if self.log is not None:
            self.log.close()
        subprocess.run(["adb", "kill-server"], capture_output=True)

    @staticmethod
    def _free_port():
        with socket.socket() as s:
            s.bind(("127.0.0.1", 0))
            return s.getsockname()[1]
