/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.vpnservice

import android.annotation.SuppressLint
import android.content.Intent
import android.net.LocalServerSocket
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import java.io.FileNotFoundException
import java.security.AccessControlException
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.apache.commons.configuration2.ex.ConversionException
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.config.TincConfiguration
import org.pacien.tincapp.config.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import org.pacien.tincapp.extensions.Java.defaultMessage
import org.pacien.tincapp.vpnservice.VpnServiceBuilderExtensions.applyCfg
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.keyring.TincKeyring
import org.slf4j.LoggerFactory
import org.pacien.tincapp.repository.VpnEvent
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState
import kotlin.time.Duration.Companion.milliseconds

/**
 * @author euxane
 */
@SuppressLint("VpnServicePolicy")
class TincVpnService : VpnService() {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val connectivityChangeReceiver = ConnectivityChangeReceiver
  private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private val vpnMutex = Mutex()

  override fun onDestroy() {
    serviceScope.launch {
      stopVpn()
      serviceScope.cancel()
    }
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    log.info("Intent received: {}", intent.toString())

    serviceScope.launch {
      when (intent.action) {
        Actions.ACTION_CONNECT if intent.scheme == Actions.TINC_SCHEME ->
          startVpn(intent.data!!.schemeSpecificPart, intent.data!!.fragment)

        Actions.ACTION_DISCONNECT ->
          stopVpn()

        Actions.ACTION_SYSTEM_CONNECT ->
          restorePreviousConnection()

        else -> log.error("Invalid intent action received: ${intent.action}")
      }
    }

    return START_NOT_STICKY
  }

  private suspend fun restorePreviousConnection() {
    val netName = VpnRepository.getNetName()
    if (netName == null) {
      log.info("No connection to restore.")
      return
    }

    log.info("Restoring previous connection to \"$netName\".")
    startVpn(netName, VpnRepository.getPassphrase())
  }

  private suspend fun startVpn(netName: String, passphrase: String? = null): Unit = vpnMutex.withLock {
    startVpnLocked(netName, passphrase)
  }

  private suspend fun startVpnLocked(netName: String, passphrase: String? = null) {
    if (netName.isBlank())
      return reportError(resources.getString(R.string.notification_error_message_no_network_name_provided), docTopic = "doc.html#intent-api")

    if (TincKeyring.needsPassphrase(netName) && passphrase == null)
      return reportError(resources.getString(R.string.notification_error_message_passphrase_not_provided))

    if (!AppPaths.confDir(netName).exists())
      return reportError(
        resources.getString(R.string.notification_error_message_no_configuration_for_network_format, netName),
        configDir = netName,
        docTopic = "doc.html#configuration-files"
      )

    log.info("Starting tinc daemon for network \"$netName\".")
    if (VpnRepository.currentNetName != null || VpnRepository.vpnState.value !is VpnState.Disconnected)
      stopVpnInternal()

    VpnRepository.updateState(VpnState.Connecting)

    val privateKeys = try {
      TincConfiguration.fromTincConfiguration(AppPaths.existing(AppPaths.tincConfFile(netName))).let { tincCfg ->
        Pair(
          TincKeyring.unlockKey(
            AppPaths.NET_DEFAULT_ED25519_PRIVATE_KEY_FILE,
            tincCfg.ed25519PrivateKeyFile ?: AppPaths.defaultEd25519PrivateKeyFile(netName),
            passphrase),
          TincKeyring.unlockKey(
            AppPaths.NET_DEFAULT_RSA_PRIVATE_KEY_FILE,
            tincCfg.privateKeyFile ?: AppPaths.defaultRsaPrivateKeyFile(netName),
            passphrase))
      }
    } catch (_: FileNotFoundException) {
      Pair(null, null)
    } catch (e: org.bouncycastle.openssl.PEMException) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_decrypt_private_keys_format, e.message))
    } catch (e: Exception) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_read_private_key_format, e.defaultMessage()), e)
    }

    val interfaceCfg = try {
      VpnInterfaceConfiguration.fromIfaceConfiguration(AppPaths.existing(AppPaths.netConfFile(netName)))
    } catch (e: FileNotFoundException) {
      return reportError(
        resources.getString(R.string.notification_error_message_network_config_not_found_format, e.defaultMessage()),
        e,
        configDir = netName,
        docTopic = "doc.html#configuration-files"
      )
    } catch (e: ConversionException) {
      return reportError(
        resources.getString(R.string.notification_error_message_network_config_invalid_format, e.defaultMessage()),
        e,
        configDir = netName,
        docTopic = "doc.html#network-interface",
      )
    } catch (e: Exception) {
      return reportError(
        resources.getString(R.string.notification_error_message_could_not_read_network_configuration_format, e.defaultMessage()),
        e,
        configDir = netName,
      )
    }

    val deviceFd = try {
      Builder().setSession(netName)
        .applyCfg(interfaceCfg)
        .also { applyIgnoringException(it::addDisallowedApplication, BuildConfig.APPLICATION_ID) }
        // inherit metered property from underlying network
        .also { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.setMetered(false) }
        .establish()!!
    } catch (e: IllegalArgumentException) {
      return reportError(
        resources.getString(R.string.notification_error_message_network_config_invalid_format, e.defaultMessage()),
        e,
        docTopic = "doc.html#network-interface",
        configDir = netName,
      )
    } catch (e: NullPointerException) {
      return reportError(
        resources.getString(R.string.notification_error_message_could_not_bind_iface),
        e,
        proposeLogs = true,
      )
    } catch (e: Exception) {
      return reportError(
        resources.getString(R.string.notification_error_message_could_not_configure_iface, e.defaultMessage()),
        e,
        proposeLogs = true,
      )
    }

    val serverSocket = LocalServerSocket(DEVICE_FD_ABSTRACT_SOCKET)
    serviceScope.launch { serveDeviceFd(serverSocket, deviceFd) }

    val daemonJob = serviceScope.launch(Dispatchers.IO) {
      try {
        Tincd.start(netName, DEVICE_FD_ABSTRACT_SOCKET, privateKeys.first, privateKeys.second)
      } catch (e: Exception) {
        log.error("tinc daemon exited with error", e)
      }
    }
    VpnRepository.setState(netName, passphrase, interfaceCfg, deviceFd, daemonJob)

    try {
      waitForDaemonStartup()
      log.info("tinc daemon started.")
      VpnRepository.updateState(VpnState.Connected(netName))
    } catch (e: Exception) {
      reportError(
        resources.getString(R.string.notification_error_message_daemon_exited, e.defaultMessage()),
        e,
        proposeLogs = true,
      )
      stopVpnInternal()
    } finally {
      serverSocket.close()
      deviceFd.close()
    }

    if (interfaceCfg.reconnectOnNetworkChange)
      connectivityChangeReceiver.registerWatcher(this, serviceScope)
  }

  private suspend fun stopVpn(): Unit = vpnMutex.withLock {
    stopVpnInternal()
  }

  private suspend fun stopVpnInternal() {
    log.info("Stopping any running tinc daemon.")

    connectivityChangeReceiver.unregisterWatcher()

    getCurrentNetName()?.let {
      try {
        Tinc.stop(it)
      } catch (e: Exception) {
        log.error("Error stopping tinc daemon", e)
      }
      log.info("All tinc daemons stopped.")
      VpnRepository.updateState(VpnState.Disconnected)
      VpnRepository.setState(null, null, null, null, null)
    }
  }

  private fun reportError(
    msg: String,
    e: Throwable? = null,
    docTopic: String? = null,
    configDir: String? = null,
    proposeLogs: Boolean = false,
  ) {
    if (e != null)
      log.error(msg, e)
    else
      log.error(msg)

    VpnRepository.updateState(VpnState.Disconnected)
    VpnRepository.emitEvent(
      VpnEvent.Error(
        R.string.notification_error_title_unable_to_start_tinc,
        msg,
        docTopic,
        configDir,
        proposeLogs,
      )
    )

    App.alert(
      R.string.notification_error_title_unable_to_start_tinc,
      msg,
      docTopic,
      configDir,
      proposeLogs,
    )
  }

  private suspend fun serveDeviceFd(serverSocket: LocalServerSocket, deviceFd: ParcelFileDescriptor) = withContext(Dispatchers.IO) {
    serverSocket.accept().let { socket ->
      try {
        if (socket.peerCredentials.uid != App.getApplicationInfo().uid)
          throw AccessControlException("Peer UID mismatch.")

        socket.setFileDescriptorsForSend(arrayOf(deviceFd.fileDescriptor))
        socket.outputStream.write(0) // dummy write
        socket.outputStream.flush()
      } catch (e: Exception) {
        log.error("Error while serving device fd", e)
      } finally {
        socket.close()
      }
    }
  }

  private suspend fun waitForDaemonStartup() {
    delay(SETUP_DELAY.milliseconds)
    val daemon = VpnRepository.daemon
    if (daemon != null && !daemon.isActive) {
      throw Exception("Daemon exited prematurely")
    }
  }

  companion object {
    private const val SETUP_DELAY = 500L // ms
    private const val DEVICE_FD_ABSTRACT_SOCKET = "${BuildConfig.APPLICATION_ID}.daemon.socket"

    fun getCurrentNetName(): String? = VpnRepository.currentNetName

    fun connect(netName: String, passphrase: String? = null) {
      App.notificationManager.dismissAll()

      App.getContext().startService(
        Intent(App.getContext(), TincVpnService::class.java)
          .setAction(Actions.ACTION_CONNECT)
          .setData(Actions.buildNetworkUri(netName, passphrase)))
    }

    fun disconnect() {
      App.getContext().startService(
        Intent(App.getContext(), TincVpnService::class.java)
          .setAction(Actions.ACTION_DISCONNECT))
    }
  }
}
