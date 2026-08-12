package org.pacien.tincapp.storageprovider

import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Java.defaultMessage
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * Migrates the configuration from the private storage (used before version 0.38) to the
 * user-accessible storage (through the USB storage mode).
 *
 * @author euxane
 */
class StorageMigrator {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val context by lazy { App.getContext() }

  fun migrate() {
    migrateConfigurationDirectory()
    migrateLogDirectory(context.externalCacheDir)
    migrateLogDirectory(File(context.cacheDir, "log"))  // migrated to "logs"
  }

  private fun migrateConfigurationDirectory() {
    val externalStorageDir = context.getExternalFilesDir(null) ?: return
    val oldConfigDir = File(externalStorageDir, "networks")
    if (!oldConfigDir.exists()) return

    try {
      val newConfigDir = AppPaths.confDir()
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

  private fun migrateLogDirectory(oldLogDir: File?) {
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