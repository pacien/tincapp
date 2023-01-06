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

package org.pacien.tincapp.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.databinding.ObservableBoolean
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.DataConnectionConfigurationFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication
import org.apache.ftpserver.usermanager.impl.WritePermission
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.configure.ConfigureActivity
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppNotificationManager
import org.pacien.tincapp.extensions.Java.defaultMessage
import org.pacien.tincapp.utils.PendingIntentUtils
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * FTP server service allowing a remote and local user to access and modify configuration files in
 * the application's context.
 *
 * @author pacien
 */
class ConfigurationAccessService : Service() {
  companion object {
    // Apache Mina FtpServer's INFO log level is actually VERBOSE.
    // The object holds static references to those loggers so that they stay around.
    @Suppress("unused")
    private val MINA_FTP_LOGGER_OVERRIDER = MinaLoggerOverrider(Level.WARN)

    private val context by lazy { App.getContext() }
    private val store by lazy { context.getSharedPreferences("${this::class.java.`package`!!.name}.ftp", Context.MODE_PRIVATE)!! }
    val runningState = ObservableBoolean(false)

    fun getFtpHomeDir(): String = context.applicationInfo.dataDir!!
    fun getFtpUsername() = storeGetOrInsertString("username") { "tincapp" }
    fun getFtpPassword() = storeGetOrInsertString("password") { generateRandomString(8) }
    fun getFtpPort() = storeGetOrInsertInt("port") { 65521 } // tinc port `concat` FTP port
    fun getFtpPassiveDataPorts() = storeGetOrInsertString("passive-range") { "65522-65532" }

    private fun storeGetOrInsertString(key: String, defaultGenerator: () -> String): String = synchronized(store) {
      if (!store.contains(key)) store.edit().putString(key, defaultGenerator()).apply()
      return store.getString(key, null)!!
    }

    private fun storeGetOrInsertInt(key: String, defaultGenerator: () -> Int): Int = synchronized(store) {
      if (!store.contains(key)) store.edit().putInt(key, defaultGenerator()).apply()
      return store.getInt(key, 0)
    }

    private fun generateRandomString(length: Int): String {
      val alphabet = ('a'..'z') + ('A'..'Z') + ('0'..'9')
      return List(length) { alphabet.random() }.joinToString("")
    }
  }

  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val notificationManager by lazy { App.notificationManager }
  private var sftpServer: FtpServer? = null

  override fun onBind(intent: Intent): IBinder? = null // non-bindable service

  override fun onDestroy() {
    sftpServer?.stop()
    sftpServer = null
    runningState.set(false)
    log.info("Stopped FTP server")
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val ftpUser = StaticFtpUser(getFtpUsername(), getFtpPassword(), getFtpHomeDir(), listOf(WritePermission()))
    sftpServer = setupSingleUserServer(ftpUser, getFtpPort(), getFtpPassiveDataPorts()).also {
      try {
        it.start()
        runningState.set(true)
        log.info("Started FTP server on port {}", getFtpPort())
        pinInForeground()
      } catch (e: IOException) {
        log.error("Could not start FTP server", e)
        App.alert(R.string.notification_error_title_unable_to_start_ftp_server, e.defaultMessage())
      }
    }

    return START_NOT_STICKY
  }

  /**
   * Pins the service in the foreground so that it doesn't get stopped by the system when the
   * application's activities are put in the background, which is the case when the user sets the
   * focus on an FTP client app for example.
   */
  private fun pinInForeground() {
    startForeground(
      AppNotificationManager.CONFIG_ACCESS_NOTIFICATION_ID,
      notificationManager.newConfigurationAccessNotificationBuilder()
        .setSmallIcon(R.drawable.ic_baseline_folder_open_primary_24dp)
        .setContentTitle(resources.getString(R.string.notification_config_access_server_running_title))
        .setContentText(resources.getString(R.string.notification_config_access_server_running_message))
        .setContentIntent(Intent(this, ConfigureActivity::class.java).let {
          PendingIntentUtils.getActivity(this, 0, it, 0)
        })
        .build()
    )
  }

  private fun setupSingleUserServer(ftpUser: User, ftpPort: Int, ftpPassivePorts: String): FtpServer =
    FtpServerFactory()
      .apply {
        addListener("default", ListenerFactory()
          .apply {
            connectionConfig = ConnectionConfigFactory()
              .apply { maxThreads = 1 } // library has issues with multiple threads
              .createConnectionConfig()
          }
          .apply { port = ftpPort }
          .apply {
            dataConnectionConfiguration = DataConnectionConfigurationFactory()
              .apply { passivePorts = ftpPassivePorts }
              .createDataConnectionConfiguration()
          }
          .createListener()
        )
      }
      .apply { userManager = StaticFtpUserManager(listOf(ftpUser)) }
      .createServer()

  private class StaticFtpUserManager(users: List<User>) : UserManager {
    private val userMap: Map<String, User> = users.map { it.name to it }.toMap()
    override fun getUserByName(username: String?): User? = userMap[username]
    override fun getAllUserNames(): Array<String> = userMap.keys.toTypedArray()
    override fun doesExist(username: String?): Boolean = username in userMap
    override fun delete(username: String?) = throw UnsupportedOperationException()
    override fun save(user: User?) = throw UnsupportedOperationException()
    override fun getAdminName(): String = throw UnsupportedOperationException()
    override fun isAdmin(username: String?): Boolean = throw UnsupportedOperationException()
    override fun authenticate(authentication: Authentication?): User = when (authentication) {
      is UsernamePasswordAuthentication -> getUserByName(authentication.username).let {
        if (it != null && authentication.password == it.password) it
        else throw AuthenticationFailedException()
      }
      else -> throw IllegalArgumentException()
    }
  }

  private data class StaticFtpUser(
    private val name: String,
    private val password: String,
    private val homeDirectory: String,
    private val authorities: List<Authority>
  ) : User {
    override fun getName(): String = name
    override fun getPassword(): String = password
    override fun getAuthorities(): List<Authority> = authorities
    override fun getAuthorities(clazz: Class<out Authority>): List<Authority> = authorities.filter(clazz::isInstance)
    override fun getMaxIdleTime(): Int = 0 // unlimited
    override fun getEnabled(): Boolean = true
    override fun getHomeDirectory(): String = homeDirectory
    override fun authorize(request: AuthorizationRequest?): AuthorizationRequest? =
      authorities.filter { it.canAuthorize(request) }.fold(request) { req, auth -> auth.authorize(req) }
  }

  /**
   * This registers package loggers filtering the output of the Mina FtpServer.
   * The object holds static references to those loggers so that they stay around.
   */
  private class MinaLoggerOverrider(logLevel: Level) {
    @Suppress("unused")
    private val ftpServerLogger = forceLogLevel("org.apache.ftpserver", logLevel)

    @Suppress("unused")
    private val minaLogger = forceLogLevel("org.apache.mina", logLevel)

    private fun forceLogLevel(pkgName: String, logLevel: Level) =
      (LoggerFactory.getLogger(pkgName) as Logger).apply { level = logLevel }
  }
}
