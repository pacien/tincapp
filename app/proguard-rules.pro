# tinc app, an Android binding and user interface for the tinc mesh VPN daemon
# Copyright (C) 2018 Pacien TRAN-GIRARD
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

-keep class org.bouncycastle.**
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
-dontobfuscate
-dontwarn org.apache.commons.**
-dontwarn org.bouncycastle.**
-dontwarn ch.qos.logback.core.net.*
-dontwarn sun.misc.Unsafe
-dontwarn build.IgnoreJava8API
