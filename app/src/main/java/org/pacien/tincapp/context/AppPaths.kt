/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2023 Pacien TRAN-GIRARD
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

import java.io.File
import java.io.FileNotFoundException

/**
 * @author pacien
 *
 * @implNote Logs and PID files are stored in the cache directory for automatic collection.
 */
object AppPaths {
  private const val APP_LOG_DIR = "log"
  private const val APP_TINC_RUNTIME_DIR = "run"
  private const val APP_TINC_NETWORKS_DIR = "networks"

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

  const val NET_DEFAULT_ED25519_PRIVATE_KEY_FILE = "ed25519_key.priv"
  const val NET_DEFAULT_RSA_PRIVATE_KEY_FILE = "rsa_key.priv"

  private val context by lazy { App.getContext() }

  private fun privateCacheDir() = context.cacheDir!!
  private fun publicCacheDir() = context.externalCacheDir!!
  private fun publicFilesDir() = context.getExternalFilesDir(null)
  private fun binDir() = File(context.applicationInfo.nativeLibraryDir)

  fun runtimeDir() = withDir(File(privateCacheDir(), APP_TINC_RUNTIME_DIR))
  fun logDir() = withDir(File(publicCacheDir(), APP_LOG_DIR))
  fun confDir() = withDir(File(publicFilesDir(), APP_TINC_NETWORKS_DIR))

  fun confDir(netName: String) = File(confDir(), netName)
  fun hostsDir(netName: String) = File(confDir(netName), NET_HOSTS_DIR)
  fun netConfFile(netName: String) = File(confDir(netName), NET_CONF_FILE)
  fun tincConfFile(netName: String) = File(confDir(netName), NET_TINC_CONF_FILE)
  fun invitationFile(netName: String) = File(confDir(netName), NET_INVITATION_FILE)
  fun logFile(netName: String) = File(logDir(), String.format(LOGFILE_FORMAT, netName))
  fun pidFile(netName: String) = File(runtimeDir(), String.format(PIDFILE_FORMAT, netName))
  fun appLogFile() = File(logDir(), APPLOG_FILE)
  fun crashFlagFile() = File(privateCacheDir(), CRASHFLAG_FILE)

  fun existing(f: File) = f.apply { if (!exists()) throw FileNotFoundException(f.absolutePath) }
  fun withDir(f: File) = f.apply { if (!exists()) mkdirs() }

  fun defaultEd25519PrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_ED25519_PRIVATE_KEY_FILE)
  fun defaultRsaPrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_RSA_PRIVATE_KEY_FILE)

  fun tincd() = File(binDir(), TINCD_BIN)
  fun tinc() = File(binDir(), TINC_BIN)
}
