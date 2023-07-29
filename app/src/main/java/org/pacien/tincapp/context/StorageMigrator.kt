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

import org.pacien.tincapp.extensions.Java.defaultMessage
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Migrates the configuration from the private storage (used before version 0.38) to the
 * user-accessible storage (through the USB storage mode).
 *
 * @author pacien
 */
class StorageMigrator {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val context by lazy { App.getContext() }

  fun migrate() {
    migrateConfigurationDirectory()
    migrateLogDirectory()
  }

  private fun migrateConfigurationDirectory() {
    val oldConfigDir = context.filesDir
    if (oldConfigDir == null || oldConfigDir.listFiles().isNullOrEmpty()) return // nothing to do

    try {
      val newConfigDir = context.getExternalFilesDir(null)!!
      log.info(
        "Migrating files present in old configuration directory at {} to {}",
        oldConfigDir.absolutePath,
        newConfigDir.absolutePath
      )

      oldConfigDir.copyRecursively(newConfigDir, overwrite = false)
      oldConfigDir.deleteRecursively()
    } catch (e: IOException) {
      log.warn("Could not complete configuration directory migration: {}", e.defaultMessage())
    }
  }

  private fun migrateLogDirectory() {
    val oldLogDir = context.cacheDir
    if (oldLogDir == null || oldLogDir.listFiles().isNullOrEmpty()) return // nothing to do

    try {
      // There's no point moving the log files. Let's delete those instead.
      log.info("Clearing old cache directory at {}", oldLogDir.absolutePath)
      oldLogDir.deleteRecursively()
    } catch (e: IOException) {
      log.warn("Could not remove old cache directory: {}", e.defaultMessage())
    }
  }
}
