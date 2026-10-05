#!/usr/bin/env python3
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

"""Runs the app's instrumented tests on a fresh emulated device."""

import argparse
import contextlib
import subprocess
import sys
import tempfile
from pathlib import Path

from android_device import APP_PACKAGE, Emulator

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("apk", type=Path, help="debug APK of the app")
parser.add_argument("test_apk", type=Path, help="instrumented test APK")
parser.add_argument("--report", type=Path, help="directory for the report, screenshots and screen recording")
args = parser.parse_args()

work_dir = Path(tempfile.mkdtemp(prefix="tincapp-instrumented-tests-"))
emulator = Emulator(work_dir)
try:
    device = emulator.start()
    device.install_app(args.apk)
    device.adb("install", "-r", "-t", str(args.test_apk))
    with contextlib.ExitStack() as context:
        if args.report:
            args.report.mkdir(parents=True, exist_ok=True)
            context.enter_context(device.recording(args.report / "recording.mp4"))
        # Tests with real peers are run by the end-to-end VPN test instead.
        passed, report = device.instrument(f"{APP_PACKAGE}.test", {"notClass": f"{APP_PACKAGE}.PeerNetworkTest"})
    print(report)
    if args.report and passed:
        (args.report / "report.txt").write_text(report)
        device.pull_screenshots(args.report)
    if not passed:
        device.pull_screenshots(work_dir)
        device.save_diagnostics(work_dir)
        subprocess.run(["chmod", "-R", "a+rX", str(work_dir)])  # for inspection with nix build --keep-failed
        for log in ["app.log", "crash.txt"]:
            print((work_dir / log).read_text())
        sys.exit(1)
finally:
    emulator.stop()
