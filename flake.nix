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

{
  description = "Tinc Mesh VPN: Android client and user interface";

  inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-26.05";

  outputs =
    { self, nixpkgs }:
    let
      system = "x86_64-linux";

      pkgs = import nixpkgs {
        inherit system;
        config = {
          allowUnfree = true;
          android_sdk.accept_license = true;
        };
      };

      # Keep in sync with app/build.gradle and Android Gradle Plugin defaults
      platformVersion = "37.0";
      buildToolsVersion = "36.0.0";
      ndkVersion = "29.0.14206865";
      cmakeVersion = "3.22.1";

      android = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ platformVersion ];
        buildToolsVersions = [ buildToolsVersion ];
        includeNDK = true;
        ndkVersions = [ ndkVersion ];
        includeCmake = true;
        cmakeVersions = [ cmakeVersion ];

        # For Android Studio's device manager and instrumented tests
        includeEmulator = true;
        includeSystemImages = true;
        systemImageTypes = [ "google_apis" ];
        abiVersions = [ "x86_64" ];
      };

      androidHome = "${android.androidsdk}/libexec/android-sdk";

      # Separate SDK for the automated tests on an AOSP system image
      # (without Google apps and their background setup making things flaky).
      # (Note: AOSP image releases are lagging by one API level.)
      testAndroid = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "36" ];
        includeCmake = false;
        includeEmulator = true;
        includeSystemImages = true;
        systemImageTypes = [ "default" ];
        abiVersions = [ "x86_64" ];
      };
      testAndroidHome = "${testAndroid.androidsdk}/libexec/android-sdk";
      testSystemImage = "system-images;android-36;default;x86_64";

      vpnTest =
        withReport:
        import ./tests/vpn/vpn-test.nix {
          inherit pkgs jdk debugApk withReport;
          androidSdk = testAndroid.androidsdk;
          androidHome = testAndroidHome;
          systemImage = testSystemImage;
        };

      # Instrumented tests (app/src/androidTest), on a fresh emulated device
      instrumentedTests =
        withReport:
        pkgs.runCommand "instrumented-tests${pkgs.lib.optionalString withReport "-report"}"
          {
            nativeBuildInputs = [
              pkgs.python3
              testAndroid.androidsdk
              jdk
            ];
            requiredSystemFeatures = [ "kvm" ];
            ANDROID_HOME = testAndroidHome;
            JAVA_HOME = jdk.home;
            TINCAPP_TEST_SYSTEM_IMAGE = testSystemImage;
            PYTHONPATH = pkgs.lib.fileset.toSource {
              root = ./tests;
              fileset = ./tests/android_device.py;
            };
          }
          ''
            export HOME="$TMPDIR"
            python3 ${./tests/instrumented_tests.py} \
              ${debugApk}/app-debug.apk ${debugApk}/app-debug-androidTest.apk \
              ${if withReport then "--report $out" else "&& touch $out"}
          '';
      aapt2 = "${androidHome}/build-tools/${buildToolsVersion}/aapt2";
      jdk = pkgs.jdk21;
      gradle = pkgs.gradle_9;

      # Host tools for the native deps built through CMake ExternalProject
      nativeHostTools = with pkgs; [
        autoconf
        automake
        libtool
        gnumake
        m4
        perl
      ];

      # Source archives of the native deps, otherwise downloaded by CMake
      nativeSourceArchives = pkgs.linkFarmFromDrvs "tincapp-native-source-archives" (
        map (source: pkgs.fetchurl { inherit (source) url sha256; }) (
          builtins.attrValues (builtins.fromJSON (builtins.readFile ./app/native-sources.json))
        )
      );

      debugApk = pkgs.stdenvNoCC.mkDerivation (finalAttrs: {
        pname = "tincapp-debug-apk";
        version = "dev";

        src = pkgs.lib.fileset.toSource {
          root = ./.;
          fileset = pkgs.lib.fileset.unions [
            ./app
            ./gradle
            ./build.gradle
            ./settings.gradle
            ./gradle.properties
          ];
        };

        nativeBuildInputs = [
          gradle
          jdk
        ]
        ++ nativeHostTools;

        # Update with: nix build .#debug-apk.mitmCache.updateScript && ./result
        mitmCache = gradle.fetchDeps {
          pkg = finalAttrs.finalPackage;
          data = ./nix/debug-apk-deps.json;
          useBwrap = false;
        };

        gradleBuildTask = ":app:assembleDebug :app:assembleDebugAndroidTest";
        gradleUpdateTask = finalAttrs.gradleBuildTask;
        gradleFlags = [
          "-Dorg.gradle.java.home=${jdk.home}"
          "-Pandroid.aapt2FromMavenOverride=${aapt2}"
        ];

        env = {
          ANDROID_HOME = androidHome;
          TINCAPP_SOURCE_ARCHIVES_DIR = nativeSourceArchives;
        };

        # Writable Android user directory
        preBuild = ''
          export ANDROID_USER_HOME="$(mktemp -d)"
        '';

        installPhase = ''
          install -D app/build/outputs/apk/debug/app-debug.apk "$out/app-debug.apk"
          install -D app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
            "$out/app-debug-androidTest.apk"
        '';

        dontFixup = true;
      });
    in
    {
      # Reproducible test results
      checks.${system} = {
        vpn-test = vpnTest false;
        instrumented-tests = instrumentedTests false;
      };

      # Test reports with screenshots and screen recordings, not reproducible
      packages.${system} = {
        debug-apk = debugApk;
        vpn-test-report = vpnTest true;
        instrumented-tests-report = instrumentedTests true;
      };

      devShells.${system}.default = pkgs.mkShellNoCC {
        packages = [
          jdk
          android.androidsdk
        ]
        ++ nativeHostTools;

        JAVA_HOME = jdk.home;
        ANDROID_HOME = androidHome;

        # Use patched aapt2 from the SDK instead of generic one fetched from
        # Maven, which does not run on NixOS.
        GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${aapt2}";

        # The Android Gradle Plugin prefers sdk.dir from local.properties over
        # ANDROID_HOME.
        # Point it to this SDK, also for Android Studio, keeping any other
        # local property.
        shellHook = ''
          loc=$(git rev-parse --show-toplevel 2>/dev/null || pwd)
          local_properties="$loc/local.properties"
          touch "$local_properties"
          sed -i '/^sdk\.dir=/d' "$local_properties"
          echo "sdk.dir=${androidHome}" >> "$local_properties"
        '';
      };
    };
}
