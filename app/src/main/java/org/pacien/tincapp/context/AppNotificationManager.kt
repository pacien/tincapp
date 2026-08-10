/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2024 Euxane P. TRAN-GIRARD
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

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit

/**
 * @author euxane
 */
class AppNotificationManager(private val context: Context) {
  data class ErrorNotification(
    val title: String,
    val message: String,
    val manualLink: String?,
    val configDir: String?,
    val proposeLogs: Boolean,
  )

  companion object {
    private val STORE_NAME = this::class.java.`package`!!.name
    private const val STORE_KEY_TITLE = "title"
    private const val STORE_KEY_MESSAGE = "message"
    private const val STORE_KEY_MANUAL_LINK = "manual_link"
    private const val STORE_CONFIG_DIR = "config_dir"
    private const val STORE_PROPOSE_LOGS = "propose_logs_dir"
  }

  private val store by lazy { context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)!! }

  fun getError(): ErrorNotification? {
    if (!store.contains(STORE_KEY_TITLE)) return null

    return ErrorNotification(
      store.getString(STORE_KEY_TITLE, null)!!,
      store.getString(STORE_KEY_MESSAGE, null)!!,
      store.getString(STORE_KEY_MANUAL_LINK, null),
      store.getString(STORE_CONFIG_DIR, null),
      store.getBoolean(STORE_PROPOSE_LOGS, false),
    )
  }

  fun notifyError(
    title: String,
    message: String,
    manualLink: String? = null,
    configDir: String? = null,
    proposeLogs: Boolean = false,
  ) {
    store
      .edit {
        putString(STORE_KEY_TITLE, title)
          .putString(STORE_KEY_MESSAGE, message)
          .putString(STORE_KEY_MANUAL_LINK, manualLink)
          .putString(STORE_CONFIG_DIR, configDir)
          .putBoolean(STORE_PROPOSE_LOGS, proposeLogs)
      }
  }

  fun dismissAll() {
    store
      .edit {
          clear()
      }
  }

  fun registerListener(listener: OnSharedPreferenceChangeListener) {
    store.registerOnSharedPreferenceChangeListener(listener)
  }

  fun unregisterListener(listener: OnSharedPreferenceChangeListener) {
    store.unregisterOnSharedPreferenceChangeListener(listener)
  }
}
