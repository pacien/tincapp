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

# End-to-end VPN test between a NixOS "computer" node and the app on an
# emulated Android device.
#
# The emulator runs next to the computer's VM (not nested), and reaches the
# computer node through the emulator's NAT (10.0.2.2: the host loopback) and
# the VM's port forwarding.

{
  pkgs,
  androidSdk,
  androidHome,
  systemImage,
  jdk,
  debugApk,
}:

let
  netName = "testnet";
  computerAddress = "10.0.0.1";
  androidAddress = "10.0.0.2";
  forwardedPort = 6655; # on the host, to the computer's tinc port 655

  # Throwaway keys, committed for reproducibility
  hostFile = name: address: builtins.readFile ./keys/${name}/host + "Subnet = ${address}/32\n";
  computerHost = hostFile "computer" computerAddress;
  androidHost = hostFile "android" androidAddress;

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
    Address = ${androidAddress}/32
    Route = 10.0.0.0/24
    EOF
    cat > $net/hosts/android <<EOF
    ${androidHost}Port = 1655
    EOF
    cat > $net/hosts/computer <<EOF
    ${computerHost}Address = 10.0.2.2 ${toString forwardedPort}
    EOF
  '';

  androidDeviceModule =
    python3Packages:
    python3Packages.toPythonModule (
      pkgs.runCommand "android-device-test-module" { } ''
        install -D ${../android_device.py} $out/${python3Packages.python.sitePackages}/android_device.py
      ''
    );
in
pkgs.testers.runNixOSTest {
  name = "tincapp-vpn";

  extraPythonPackages = p: [ (androidDeviceModule p) ];

  nodes.computer = {
    services.tinc.networks.${netName} = {
      name = "computer";
      ed25519PrivateKeyFile = "${./keys/computer/ed25519_key.priv}";
      rsaPrivateKeyFile = "${./keys/computer/rsa_key.priv}";
      hosts = {
        computer = computerHost;
        android = androidHost;
      };
      settings = {
        Mode = "router";
        AddressFamily = "ipv4";
      };
      debugLevel = 3;
    };

    networking.interfaces."tinc.${netName}".ipv4.addresses = [
      {
        address = computerAddress;
        prefixLength = 24;
      }
    ];

    networking.firewall = {
      allowedTCPPorts = [ 655 ];
      allowedUDPPorts = [ 655 ];
    };

    virtualisation.forwardPorts = map (proto: {
      inherit proto;
      host.port = forwardedPort;
      guest.port = 655;
    }) [ "tcp" "udp" ];
  };

  testScript = ''
    import os, tempfile
    from pathlib import Path

    os.environ["PATH"] = "${pkgs.lib.makeBinPath [ androidSdk jdk pkgs.gnutar ]}:" + os.environ["PATH"]
    os.environ["ANDROID_HOME"] = "${androidHome}"
    os.environ["JAVA_HOME"] = "${jdk.home}"
    os.environ["TINCAPP_TEST_SYSTEM_IMAGE"] = "${systemImage}"
    from android_device import Emulator

    tinc = "tinc.${netName}"
    work_dir = Path(tempfile.mkdtemp(prefix="tincapp-vpn-test-"))
    os.environ["HOME"] = str(work_dir)  # adb and emulator console keys
    emulator = Emulator(work_dir)

    computer.wait_for_unit("tinc.${netName}.service")

    try:
        with subtest("start the Android device with the app and its network"):
            device = emulator.start()
            device.install_app("${debugApk}/app-debug.apk")
            device.install_network(Path("${androidNetwork}/${netName}"))

        with subtest("connect through the Intent API"):
            device.connect("${netName}", "${androidAddress}")
            computer.wait_until_succeeds(f"{tinc} dump reachable nodes | grep -q '^android '", timeout=120)

        with subtest("computer reaches android"):
            computer.succeed("ping -c 3 -W 5 ${androidAddress}")

        with subtest("android reaches computer"):
            print(device.ping("${computerAddress}"))

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
        print(computer.execute(f"{tinc} info android; journalctl -u tinc.${netName} --no-pager | tail -50")[1])
        raise
    finally:
        emulator.stop()
  '';
}
