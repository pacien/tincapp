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

# End-to-end tests of the app on an emulated Android device, in a tinc mesh
# with two NixOS nodes:
#
#   android (emulator) --- computer (VM) --- laptop (VM)
#        10.0.2.2:6655 -> 655       virtual LAN
#
# The emulator runs next to the VMs (not nested), and reaches the computer
# through the emulator's NAT (10.0.2.2: the host loopback) and the computer VM's
# port forwarding. The laptop is only reachable through the computer.

{
  pkgs,
  androidSdk,
  androidHome,
  systemImage,
  jdk,
  debugApk,
  withReport ? false, # screenshots and screen recording, making the output not reproducible
}:

let
  netName = "testnet";
  forwardedPort = 6655; # on the host, to the computer's tinc port 655
  testPort = 7000; # for TCP and UDP traffic tests
  addresses = {
    computer = "10.0.0.1";
    android = "10.0.0.2";
    laptop = "10.0.0.3";
    phone = "10.0.0.4"; # joining through an invitation
  };

  # Throwaway keys, committed for reproducibility
  hostFile = name: builtins.readFile ./keys/${name}/host + "Subnet = ${addresses.${name}}/32\n";

  # Address of the computer as seen from the emulator
  computerHostForAndroid = hostFile "computer" + "Address = 10.0.2.2 ${toString forwardedPort}\n";

  androidNetwork = pkgs.runCommand "tincapp-test-network" { } ''
    net=$out/${netName}
    mkdir -p $net/hosts
    cp ${./keys/android}/*.priv $net/
    cat > $net/tinc.conf <<EOF
    Name = android
    Mode = router
    AddressFamily = ipv4
    ConnectTo = computer
    EOF
    cat > $net/network.conf <<EOF
    Address = ${addresses.android}/32
    Route = 10.0.0.0/24
    EOF
    cat > $net/hosts/android <<EOF
    ${hostFile "android"}Port = 1655
    EOF
    cat > $net/hosts/computer <<EOF
    ${computerHostForAndroid}
    EOF
  '';

  # Adds the interface configuration of the invited node to its invitation,
  # before the inviter's host file.
  invitationCreatedScript = pkgs.writeShellScript "invitation-created" ''
    ${pkgs.gnused}/bin/sed -i \
      '0,/^#---/s//Ifconfig = ${addresses.phone}\/32\nRoute = 10.0.0.0\/24\nSubnet = ${addresses.phone}\/32\n&/' \
      "$INVITATION_FILE"
  '';

  tincNode =
    name: hosts:
    { pkgs, ... }:
    {
      services.tinc.networks.${netName} = {
        inherit name hosts;
        ed25519PrivateKeyFile = "${./keys/${name}/ed25519_key.priv}";
        rsaPrivateKeyFile = "${./keys/${name}/rsa_key.priv}";
        settings = {
          Mode = "router";
          AddressFamily = "ipv4";
        };
        debugLevel = 3;
      };

      networking.interfaces."tinc.${netName}".ipv4.addresses = [
        {
          address = addresses.${name};
          prefixLength = 24;
        }
      ];

      networking.firewall = {
        allowedTCPPorts = [
          655
          testPort
        ];
        allowedUDPPorts = [
          655
          testPort
        ];
      };

      environment.systemPackages = [ pkgs.netcat-openbsd ];
    };

  androidDeviceModule =
    python3Packages:
    python3Packages.toPythonModule (
      pkgs.runCommand "android-device-test-module" { } ''
        install -D ${../android_device.py} $out/${python3Packages.python.sitePackages}/android_device.py
      ''
    );
in
pkgs.testers.runNixOSTest {
  name = "tincapp-vpn${pkgs.lib.optionalString withReport "-report"}";

  extraPythonPackages = p: [ (androidDeviceModule p) ];

  nodes.computer = {
    imports = [
      (tincNode "computer" {
        computer = computerHostForAndroid;
        android = hostFile "android";
        laptop = hostFile "laptop";
      })
    ];

    environment.etc."tinc/${netName}/invitation-created" = {
      source = invitationCreatedScript;
      mode = "0755";
    };

    virtualisation.forwardPorts = map (proto: {
      inherit proto;
      host.port = forwardedPort;
      guest.port = 655;
    }) [ "tcp" "udp" ];
  };

  nodes.laptop =
    { nodes, ... }:
    {
      imports = [
        (tincNode "laptop" {
          computer = hostFile "computer" + "Address = ${nodes.computer.networking.primaryIPAddress}\n";
          android = hostFile "android";
          laptop = hostFile "laptop";
        })
      ];

      services.tinc.networks.${netName}.settings.ConnectTo = "computer";
    };

  testScript = ''
    import contextlib, os, subprocess, tempfile
    from pathlib import Path

    os.environ["PATH"] = "${pkgs.lib.makeBinPath [ androidSdk jdk pkgs.gnutar ]}:" + os.environ["PATH"]
    os.environ["ANDROID_HOME"] = "${androidHome}"
    os.environ["JAVA_HOME"] = "${jdk.home}"
    os.environ["TINCAPP_TEST_SYSTEM_IMAGE"] = "${systemImage}"
    from android_device import APP_PACKAGE, Emulator, wait_until

    tinc = "tinc.${netName}"
    with_report = ${if withReport then "True" else "False"}
    work_dir = Path(tempfile.mkdtemp(prefix="tincapp-vpn-test-"))
    os.environ["HOME"] = str(work_dir)  # adb and emulator console keys
    emulator = Emulator(work_dir)

    def receive(node, protocol):
        """Starts a one-shot listener on the node, returning a function waiting for its received data."""
        flags = "-u" if protocol == "udp" else ""
        node.succeed(f"rm -f /tmp/received; (timeout 60 nc {flags} -l ${toString testPort} > /tmp/received 2>&1 &)")
        node.wait_until_succeeds(f"ss -ln{protocol[0]} | grep -q ':${toString testPort} '", timeout=10)
        return lambda: node.wait_until_succeeds("test -s /tmp/received && cat /tmp/received", timeout=30)

    def send_from_device(address, protocol, message):
        flags = "-u" if protocol == "udp" else ""
        device.shell(f"echo {message} | toybox nc {flags} -q 1 {address} ${toString testPort}")

    def send_to_device(node, protocol, message):
        """Sends a message from the node to a one-shot listener on the device, returning what it received."""
        listen_flags = "-u" if protocol == "udp" else ""
        listener = subprocess.Popen(
            ["adb", "-s", device.serial, "shell", f"timeout 10 toybox nc {listen_flags} -l -p ${toString testPort}"],
            stdout=subprocess.PIPE, text=True)
        wait_until("device listening", lambda: ":${toString testPort} " in device.shell(f"netstat -ln{protocol[0]}"), timeout=10)
        send_flags = "-u -w 1" if protocol == "udp" else "-N"
        node.succeed(f"echo {message} | nc {send_flags} ${addresses.android} ${toString testPort}")
        return listener.communicate(timeout=30)[0]

    def ping(source, address):
        """Pings until a reply comes back: the first packets are dropped while nodes negotiate their keys."""
        if source == "android":
            wait_until(f"reply from {address}", lambda: try_device_ping(address), timeout=60)
        else:
            source.wait_until_succeeds(f"ping -c 1 -W 2 {address}", timeout=60)

    def try_device_ping(address):
        try:
            device.ping(address, count=1)
            return True
        except RuntimeError:
            return False

    start_all()
    computer.wait_for_unit("tinc.${netName}.service")
    laptop.wait_for_unit("tinc.${netName}.service")
    laptop.wait_until_succeeds(f"{tinc} dump reachable nodes | grep -q '^computer '", timeout=60)

    try:
        with subtest("start the Android device with the app"):
            device = emulator.start()
            device.install_app("${debugApk}/app-debug.apk")
            device.adb("install", "-r", "-t", "${debugApk}/app-debug-androidTest.apk")

        with subtest("UI: join through an invitation, connect, see the mesh, disconnect"):
            url = computer.succeed(f"runuser -u tinc-${netName} -- {tinc} invite phone").strip()
            computer.succeed("systemctl reload tinc.${netName}.service")  # load the new invitation key
            report_dir = Path(os.environ.get("out", work_dir)) / "peer-network-ui"
            with contextlib.ExitStack() as context:
                if with_report:
                    report_dir.mkdir(parents=True, exist_ok=True)
                    context.enter_context(device.recording(report_dir / "recording.mp4"))
                passed, report = device.instrument(f"{APP_PACKAGE}.test", {
                    "class": f"{APP_PACKAGE}.PeerNetworkTest",
                    "invitationUrl": url,
                    "address": "${addresses.phone}",
                    "computerAddress": "${addresses.computer}",
                    "laptopAddress": "${addresses.laptop}",
                })
            print(report)
            if with_report:
                (report_dir / "report.txt").write_text(report)
                device.pull_screenshots(report_dir)
            assert passed and "OK (1 test)" in report, "instrumented peer network test failed"

        with subtest("connect a static network through the Intent API"):
            device.install_network(Path("${androidNetwork}/${netName}"))
            device.connect("${netName}", "${addresses.android}")
            computer.wait_until_succeeds(f"{tinc} dump reachable nodes | grep -q '^android '", timeout=120)
            laptop.wait_until_succeeds(f"{tinc} dump reachable nodes | grep -q '^android '", timeout=60)

        with subtest("ping between android and computer"):
            ping(computer, "${addresses.android}")
            ping("android", "${addresses.computer}")

        with subtest("ping between android and laptop, through the mesh"):
            ping(laptop, "${addresses.android}")
            ping("android", "${addresses.laptop}")
            print(laptop.succeed(f"{tinc} info android"))

        for protocol in ["tcp", "udp"]:
            with subtest(f"{protocol} from android to laptop"):
                received = receive(laptop, protocol)
                send_from_device("${addresses.laptop}", protocol, f"hello-laptop-{protocol}")
                assert f"hello-laptop-{protocol}" in received()

            with subtest(f"{protocol} from computer to android"):
                assert f"hello-android-{protocol}" in send_to_device(computer, protocol, f"hello-android-{protocol}")

        # Informational: through the two QEMU user-mode NATs (emulator and VM),
        # tinc falls back to TCP, UDP probes not making it through.
        print(computer.succeed(f"{tinc} info android"))
    except Exception:
        emulator_log = emulator.work_dir / "emulator.log"
        if emulator_log.exists():
            print("".join(emulator_log.read_text().splitlines(keepends=True)[-40:]))
        if "device" in globals():
            device.save_diagnostics(emulator.work_dir)
            for log_file in ["app.log", "crash.txt"]:
                print((emulator.work_dir / log_file).read_text())
        for node in [computer, laptop]:
            print(node.execute(f"{tinc} dump nodes; journalctl -u tinc.${netName} --no-pager | tail -50")[1])
        raise
    finally:
        emulator.stop()
  '';
}
