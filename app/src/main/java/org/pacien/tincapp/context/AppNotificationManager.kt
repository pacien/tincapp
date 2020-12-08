/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.pacien.tincapp.R

/**
 * @author pacien
 */
class AppNotificationManager(private val context: Context) {
  companion object {
    private const val ERROR_CHANNEL_ID = "org.pacien.tincapp.notification.channels.error"
    private const val CONFIG_ACCESS_CHANNEL_ID = "org.pacien.tincapp.notification.channels.configuration"

    const val ERROR_NOTIFICATION_ID = 0
    const val CONFIG_ACCESS_NOTIFICATION_ID = 1
  }

  init {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) registerChannels()
  }

  fun notifyError(title: String, message: String, manualLink: String? = null) {
    val notification = NotificationCompat.Builder(context, ERROR_CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_warning_primary_24dp)
      .setContentTitle(title)
      .setContentText(message)
      .setStyle(NotificationCompat.BigTextStyle().bigText(message))
      .setHighPriority()
      .setAutoCancel(true)
      .apply { if (manualLink != null) setManualLink(manualLink) }
      .build()

    NotificationManagerCompat.from(context)
      .notify(ERROR_NOTIFICATION_ID, notification)
  }

  fun dismissAll() {
    NotificationManagerCompat.from(context).cancelAll()
  }

  fun newConfigurationAccessNotificationBuilder() =
    NotificationCompat.Builder(context, CONFIG_ACCESS_CHANNEL_ID)

  @RequiresApi(Build.VERSION_CODES.O)
  private fun registerChannels() {
    context.getSystemService(NotificationManager::class.java)
      .apply {
        createNotificationChannel(NotificationChannel(
          ERROR_CHANNEL_ID,
          context.getString(R.string.notification_error_channel_name),
          NotificationManager.IMPORTANCE_HIGH
        ))
      }
      .apply {
        createNotificationChannel(NotificationChannel(
          CONFIG_ACCESS_CHANNEL_ID,
          context.getString(R.string.notification_config_access_channel_name),
          NotificationManager.IMPORTANCE_MIN
        ))
      }
  }

  private fun NotificationCompat.Builder.setHighPriority() = apply {
    priority = NotificationCompat.PRIORITY_MAX
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) setDefaults(NotificationCompat.DEFAULT_SOUND) // force heads-up notification
  }

  private fun NotificationCompat.Builder.setManualLink(manualLink: String) = apply {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(manualLink))
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
    addAction(R.drawable.ic_help_primary_24dp, context.getString(R.string.notification_error_action_open_manual), pendingIntent)
  }
}
