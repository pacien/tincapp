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
      };

      androidHome = "${android.androidsdk}/libexec/android-sdk";
      jdk = pkgs.jdk21;
    in
    {
      devShells.${system}.default = pkgs.mkShellNoCC {
        packages = [
          jdk
          android.platform-tools

          # Host tools for the native deps built through CMake ExternalProject
          pkgs.autoconf
          pkgs.automake
          pkgs.libtool
          pkgs.gnumake
          pkgs.m4
          pkgs.perl
        ];

        JAVA_HOME = jdk.home;
        ANDROID_HOME = androidHome;

        # Use patched aapt2 from the SDK instead of generic one fetched from
        # Maven, which does not run on NixOS.
        GRADLE_OPTS =
          "-Dorg.gradle.project.android.aapt2FromMavenOverride="
          + "${androidHome}/build-tools/${buildToolsVersion}/aapt2";

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
