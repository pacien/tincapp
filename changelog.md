# Changelog

This file lists notable changes that have been made to the application on each release.
Releases are tracked and referred to using git tags.

## v0.38 -- (upcoming release)
- make configuration files and logs accessible in the user-accessible storage
  (in USB storage mode). The embedded FTP server has been removed
- display errors on the home screen instead of through the system notifications
  (as recommended-required for Android 13, API 33)

## v0.37 -- 2023-01-30
- add russian translation (contributed by exclued)

## v0.36 -- 2023-01-09
- inherit metered network restriction from underlying link (android 10+)

## v0.35 -- 2023-01-06
- fix app crash when connecting or enabling FTP server (android 12+)

## v0.34 - 2023-01-02
- add prominent warning at the top of the network list (Google Play requirement)
- update LibreSSL to 3.6.1
- update Android SDK target API to 32

## v0.33 - 2021-07-12
- update tinc to 1.1-pre18
- update LibreSSL to 3.3.3

## v0.32 - 2020-12-17
- Android 11 compatibility: expose configuration and log files through an embedded FTP server
- improve security by moving the configuration, keys and logs to a private location
- update tinc to latest snapshot (1.1-3ee0d5d)
- update LibreSSL to 3.2.2

## v0.31 - 2020-09-16
- fix app crash when external cache directory isn't available (for compatibility with Android 11)
- patch tinc for fortified libc checks (for compatibility with Android NDK r21)
- update LibreSSL to 3.1.4

## v0.30 - 2020-01-20
- fix missing system logger dependency on Android 10
- revert back to target API 28 to fix daemon not starting on Android 5

## v0.29 - 2020-01-20
- fix Android 10 compatibility issue and set target API to 29
- update tinc to patched snapshot (1.1-f522393)
- update LibreSSL to 3.0.2

## v0.28 - 2019-09-15
- fix daemon startup on Android 10
- notify user of missing VPN permission

## v0.27 - 2019-06-14
- fix R8 optimisation that made the app unable to load its libraries

## v0.26 - 2019-06-13
- make tinc automatic reconnection on network change optional with new configuration key (`ReconnectOnNetworkChange`)
- update LibreSSL to 2.9.2

## v0.25 - 2019-03-25
- implement a workaround for broken file permissions on Android-x86
- kill any remnant tinc daemon when starting a new connection
- minor UI improvements

## v0.24 - 2019-02-18
- update tinc to latest snapshot (1.1-017a7fb), fixing UDP spam
- update LibreSSL to 2.8.3
- new app icon

## v0.23 - 2018-10-08
- update tinc to 1.1pre17 (security update: CVE-2018-16737, CVE-2018-16738, CVE-2018-16758)

## v0.22 - 2018-09-27
- improve stability

## v0.21 - 2018-09-26
- force re-connection on network change
- improve stability

## v0.20 - 2018-09-09
- update existing translations
- improve assisted error reporting
- minor UI improvements

## v0.19 - 2018-08-22
- add a subnet list view
- show node reachability status
- other minor UI improvements
- embed a QR-code scanner

## v0.18 - 2018-08-07
- add support for always-on VPN
- error handling and stability improvements
- minor UI and branding improvements

## v0.17 - 2018-06-25
- update tinc to 1.1pre16
- update LibreSSL to 2.7.4
- update BCPKIX lib to 1.59

## v0.16 - 2018-06-11
- better QR-code integration
- update LibreSSL to 2.7.3
- reduce APK size

## v0.15 - 2018-05-26
- drop support for the deprecated armeabi architecture
- better error handling and reporting
- minor UI improvements

## v0.14 - 2018-04-23
- update LibreSSL to 2.7.2
- minor UI improvements

## v0.13 - 2018-03-31
- add assisted bug report feature
- minor UI improvements

## v0.12 - 2018-03-14
- better error handling
- minor UI improvements

## v0.11 - 2018-03-04
- generate a sub network configuration file when bootstrapping
- add a log viewer screen
- fix private key encryption on release versions

## v0.10 - 2018-02-24
- better error reporting
- minor UI improvements

## v0.9 - 2018-02-16
- better daemon state handling and reporting
- minor UI improvements

## v0.8 - 2018-02-10
- add Chinese translation
- update tinc to latest pre-release (1.1pre15)
- update LibreSSL to 2.6.4
- minor UI improvements
- handle unavailable external storage

## v0.7 - 2017-09-07
- add support for private key encryption using a password
- minor UI improvements
- error handling and stability improvements

## v0.6 - 2017-08-24
- update tinc to latest snapshot (1.1-92fdabc)
- add an option to join a tinc network by scanning a QR-code
- minor UI improvements

## v0.5 - 2017-08-22
- improve stability
- do not request useless permissions

## v0.4 - 2017-08-18
- update tinc to latest snapshot (1.1-7c22391)
- expose intents to allow connection and disconnection from other apps
- minor UI improvements

## v0.3 - 2017-08-03
- update tinc to latest snapshot (1.1-acefa66)
- update LibreSSL to 2.5.5
- add a connection status screen
- add an option to join a tinc network via the UI
- make external calls asynchronous

## v0.2 - 2017-07-03
- add Norwegian Bokmål and Japanese translations
- add a list of confgured tinc networks in the UI
- remove support for the MIPS architecture
- remove support for alternate configuration path
- port to Kotlin

## v0.1-preview - 2017-05-05
- basic working proof-of-concept using a patched tinc 1.1pre15
