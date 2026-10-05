Tinc Mesh VPN for Android
=========================

An Android client for tinc (<https://tinc-vpn.org/>).

* Project's homepage and documentation: <https://tincapp.euxane.eu>
* Copyright: 2017-2026 Euxane TRAN-GIRARD (+ `./contributors.md`)
* License: GPL-3.0-or-later (see `./license.md`)

* Downloads:
  * as APK on <https://tincapp.euxane.eu>
  * <https://f-droid.org/packages/org.pacien.tincapp/>
  * <https://play.google.com/store/apps/details?id=org.pacien.tincapp>

* Chat rooms:
  * Matrix: [#tincapp:matrix.org](https://matrix.to/#/#tincapp:matrix.org)
  * IRC: [#tincapp on libera.chat](https://web.libera.chat/#tincapp)

* Community translations:
  * Weblate: <https://translate.codeberg.org/projects/tincapp/>

* Code repositories:
  * <https://src.euxane.eu/tincapp/> (main, read-only)
  * <https://codeberg.org/pacien/tincapp> (mirror, merge requests accepted)
  * <https://github.com/pacien/tincapp> (mirror, merge requests accepted)

* Build commands:
  * With Gradle from the Nix flake devshell:
    * Debug APK: `./gradlew :app:assembleDebug`
    * Release APK: `./gradlew :app:assembleRelease`
    * Unit tests: `./gradlew :app:testDebugUnitTest`
    * Kotlin/Java only, without the native part:
      `./gradlew :app:compileDebugKotlin`
    * Native part for a single ABI only:
      append `-Pandroid.injected.build.abi=x86_64`
      (test-only APK, to install with `adb install -t`)
  * With Nix (sandboxed):
    * Debug APK: `nix build .#debug-apk`
    * Update locked Gradle deps (`nix/debug-apk-deps.json`):
      `nix build .#debug-apk.mitmCache.updateScript && ./result`
    * End-to-end VM test (`tests/vpn`):
      * Run: `nix build -L .#checks.x86_64-linux.vpn-test`
        (or all checks: `nix flake check`)
      * Interactive driver for debug:
        `nix run .#checks.x86_64-linux.vpn-test.driverInteractive`

* Emulator (from the Nix flake devshell):
  * Create a device: `avdmanager create avd -n tincapp -k
    'system-images;android-37.0;google_apis_ps16k;x86_64'`
  * Start it: `emulator -avd tincapp` (append `-no-window` for headless)
  * Install and run the debug APK: `./gradlew :app:installDebug`
  * Instrumented tests: `./gradlew :app:connectedDebugAndroidTest`
