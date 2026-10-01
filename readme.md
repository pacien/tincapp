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

* Build commands (from the Nix flake devshell):
  * Debug APK: `./gradlew :app:assembleDebug`
  * Release APK: `./gradlew :app:assembleRelease`
  * Unit tests: `./gradlew :app:testDebugUnitTest`
  * Kotlin/Java only, without the native part:
    `./gradlew :app:compileDebugKotlin`
  * Native part for a single ABI only: append
    `-Pandroid.injected.build.abi=x86_64`
