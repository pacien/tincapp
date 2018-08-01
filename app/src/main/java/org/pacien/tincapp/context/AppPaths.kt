/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.context

import android.os.Environment
import java.io.File
import java.io.FileNotFoundException

/**
 * @author pacien
 *
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
object AppPaths {
  private const val TINCD_BIN = "libtincd.so"
  private const val TINC_BIN = "libtinc.so"

  private const val APPLOG_FILE = "tincapp.log"
  private const val CRASHFLAG_FILE = "crash.flag"
  private const val LOGFILE_FORMAT = "tinc.%s.log"
  private const val PIDFILE_FORMAT = "tinc.%s.pid"

  private const val NET_CONF_FILE = "network.conf"
  private const val NET_TINC_CONF_FILE = "tinc.conf"
  private const val NET_HOSTS_DIR = "hosts"
  private const val NET_INVITATION_FILE = "invitation-data"
  private const val NET_DEFAULT_ED25519_PRIVATE_KEY_FILE = "ed25519_key.priv"
  private const val NET_DEFAULT_RSA_PRIVATE_KEY_FILE = "rsa_key.priv"

  fun storageAvailable() =
    Environment.getExternalStorageState().let { it == Environment.MEDIA_MOUNTED && it != Environment.MEDIA_MOUNTED_READ_ONLY }

  fun internalCacheDir() = App.getContext().cacheDir
  fun cacheDir() = App.getContext().externalCacheDir
  fun confDir() = App.getContext().getExternalFilesDir(null)
  fun binDir() = File(App.getContext().applicationInfo.nativeLibraryDir)

  fun confDir(netName: String) = File(confDir(), netName)
  fun hostsDir(netName: String) = File(confDir(netName), NET_HOSTS_DIR)
  fun netConfFile(netName: String) = File(confDir(netName), NET_CONF_FILE)
  fun tincConfFile(netName: String) = File(confDir(netName), NET_TINC_CONF_FILE)
  fun invitationFile(netName: String) = File(confDir(netName), NET_INVITATION_FILE)
  fun logFile(netName: String) = File(cacheDir(), String.format(LOGFILE_FORMAT, netName))
  fun pidFile(netName: String) = File(App.getContext().cacheDir, String.format(PIDFILE_FORMAT, netName))
  fun appLogFile() = File(cacheDir(), APPLOG_FILE)
  fun crashFlagFile() = File(internalCacheDir(), CRASHFLAG_FILE)

  fun existing(f: File) = f.apply { if (!exists()) throw FileNotFoundException(f.absolutePath) }

  fun defaultEd25519PrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_ED25519_PRIVATE_KEY_FILE)
  fun defaultRsaPrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_RSA_PRIVATE_KEY_FILE)

  fun tincd() = File(binDir(), TINCD_BIN)
  fun tinc() = File(binDir(), TINC_BIN)
}
