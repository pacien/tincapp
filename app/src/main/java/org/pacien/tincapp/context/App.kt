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

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Handler
import androidx.annotation.StringRes
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author pacien
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    appContext = applicationContext
    handler = Handler()
    AppLogger.configure()

    val logger = LoggerFactory.getLogger(this.javaClass)
    setupCrashHandler(logger)

    logger.info("Starting tinc app {} ({} build), running on {} ({})",
      BuildConfig.VERSION_NAME, BuildConfig.BUILD_TYPE, Build.VERSION.CODENAME, Build.VERSION.RELEASE)

    ConfigurationDirectoryMigrator().migrate()
  }

  private fun setupCrashHandler(logger: Logger) {
    val systemCrashHandler = Thread.getDefaultUncaughtExceptionHandler()!!
    val crashRecorder = CrashRecorder(logger, systemCrashHandler)
    Thread.setDefaultUncaughtExceptionHandler(crashRecorder)
  }

  companion object {
    private var appContext: Context? = null
    private var handler: Handler? = null

    val notificationManager: AppNotificationManager by lazy { AppNotificationManager(appContext!!) }

    fun getContext() = appContext!!
    fun getResources() = getContext().resources!!

    fun getApplicationInfo(): ApplicationInfo =
      getContext()
        .packageManager
        .getApplicationInfo(BuildConfig.APPLICATION_ID, 0)

    fun alert(@StringRes title: Int, msg: String, manualLink: String? = null) =
      notificationManager.notifyError(appContext!!.getString(title), msg, manualLink)

    fun openURL(url: String) {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      val chooser = Intent.createChooser(intent, getResources().getString(R.string.generic_action_open_web_page))
      appContext?.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // https://developer.android.com/guide/components/intents-common#Email
    fun sendMail(recipient: String, subject: String, body: String) {
      val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
        .putExtra(Intent.EXTRA_SUBJECT, subject)
        .putExtra(Intent.EXTRA_TEXT, body)

      val chooser = Intent.createChooser(intent, getResources().getString(R.string.crash_modal_action_send_email))
      appContext?.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }
}
