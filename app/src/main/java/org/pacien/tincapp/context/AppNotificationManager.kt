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

import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener

/**
 * @author pacien
 */
class AppNotificationManager(private val context: Context) {
  data class ErrorNotification(
    val title: String,
    val message: String,
    val manualLink: String?
  )

  companion object {
    private val STORE_NAME = this::class.java.`package`!!.name
    private const val STORE_KEY_TITLE = "title"
    private const val STORE_KEY_MESSAGE = "message"
    private const val STORE_KEY_MANUAL_LINK = "manual_link"
  }

  private val store by lazy { context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)!! }

  fun getError(): ErrorNotification? {
    if (!store.contains(STORE_KEY_TITLE)) return null;

    return ErrorNotification(
      store.getString(STORE_KEY_TITLE, null)!!,
      store.getString(STORE_KEY_MESSAGE, null)!!,
      store.getString(STORE_KEY_MANUAL_LINK, null)
    )
  }

  fun notifyError(title: String, message: String, manualLink: String? = null) {
    store
      .edit()
      .putString(STORE_KEY_TITLE, title)
      .putString(STORE_KEY_MESSAGE, message)
      .putString(STORE_KEY_MANUAL_LINK, manualLink)
      .apply()
  }

  fun dismissAll() {
    store
      .edit()
      .clear()
      .apply()
  }

  fun registerListener(listener: OnSharedPreferenceChangeListener) {
    store.registerOnSharedPreferenceChangeListener(listener)
  }

  fun unregisterListener(listener: OnSharedPreferenceChangeListener) {
    store.unregisterOnSharedPreferenceChangeListener(listener)
  }
}
