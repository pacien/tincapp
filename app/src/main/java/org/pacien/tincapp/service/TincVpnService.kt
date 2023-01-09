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
import android.net.LocalServerSocket
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java8.util.concurrent.CompletableFuture
import org.apache.commons.configuration2.ex.ConversionException
import org.bouncycastle.openssl.PEMException
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Executor
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.TincConfiguration
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import org.pacien.tincapp.extensions.Java.defaultMessage
import org.pacien.tincapp.extensions.VpnServiceBuilder.applyCfg
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.utils.TincKeyring
import org.slf4j.LoggerFactory
import java.io.FileNotFoundException
import java.security.AccessControlException

/**
 * @author pacien
 */
class TincVpnService : VpnService() {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val connectivityChangeReceiver = ConnectivityChangeReceiver

  override fun onDestroy() {
    stopVpn().join()
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    log.info("Intent received: {}", intent.toString())

    when {
      intent.action == Actions.ACTION_CONNECT && intent.scheme == Actions.TINC_SCHEME ->
        startVpn(intent.data!!.schemeSpecificPart, intent.data!!.fragment)
      intent.action == Actions.ACTION_DISCONNECT ->
        stopVpn()
      intent.action == Actions.ACTION_SYSTEM_CONNECT ->
        restorePreviousConnection()
      else ->
        throw IllegalArgumentException("Invalid intent action received.")
    }

    return Service.START_NOT_STICKY
  }

  private fun restorePreviousConnection() {
    val netName = getCurrentNetName()
    if (netName == null) {
      log.info("No connection to restore.")
      return
    }

    log.info("Restoring previous connection to \"$netName\".")
    startVpn(netName, getPassphrase())
  }

  private fun startVpn(netName: String, passphrase: String? = null): Unit = synchronized(this) {
    if (netName.isBlank())
      return reportError(resources.getString(R.string.notification_error_message_no_network_name_provided), docTopic = "intent-api")

    if (TincKeyring.needsPassphrase(netName) && passphrase == null)
      return reportError(resources.getString(R.string.notification_error_message_passphrase_not_provided))

    if (!AppPaths.confDir(netName).exists())
      return reportError(resources.getString(R.string.notification_error_message_no_configuration_for_network_format, netName), docTopic = "configuration")

    log.info("Starting tinc daemon for network \"$netName\".")
    if (isConnected() || getCurrentNetName() != null) stopVpn().join()

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
    } catch (e: FileNotFoundException) {
      Pair(null, null)
    } catch (e: PEMException) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_decrypt_private_keys_format, e.message))
    } catch (e: Exception) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_read_private_key_format, e.defaultMessage()), e)
    }

    val interfaceCfg = try {
      VpnInterfaceConfiguration.fromIfaceConfiguration(AppPaths.existing(AppPaths.netConfFile(netName)))
    } catch (e: FileNotFoundException) {
      return reportError(resources.getString(R.string.notification_error_message_network_config_not_found_format, e.defaultMessage()), e, "configuration")
    } catch (e: ConversionException) {
      return reportError(resources.getString(R.string.notification_error_message_network_config_invalid_format, e.defaultMessage()), e, "network-interface")
    } catch (e: Exception) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_read_network_configuration_format, e.defaultMessage()), e)
    }

    val deviceFd = try {
      Builder().setSession(netName)
        .applyCfg(interfaceCfg)
        .also { applyIgnoringException(it::addDisallowedApplication, BuildConfig.APPLICATION_ID) }
        // inherit metered property from underlying network
        .also { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) it.setMetered(false) }
        .establish()!!
    } catch (e: IllegalArgumentException) {
      return reportError(resources.getString(R.string.notification_error_message_network_config_invalid_format, e.defaultMessage()), e, "network-interface")
    } catch (e: NullPointerException) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_bind_iface), e)
    } catch (e: Exception) {
      return reportError(resources.getString(R.string.notification_error_message_could_not_configure_iface, e.defaultMessage()), e)
    }

    val serverSocket = LocalServerSocket(DEVICE_FD_ABSTRACT_SOCKET)
    Executor.runAsyncTask { serveDeviceFd(serverSocket, deviceFd) }

    val daemon = Tincd.start(netName, DEVICE_FD_ABSTRACT_SOCKET, privateKeys.first, privateKeys.second)
    setState(netName, passphrase, interfaceCfg, deviceFd, daemon)

    waitForDaemonStartup().whenComplete { _, exception ->
      serverSocket.close()
      deviceFd.close()

      if (exception != null) {
        reportError(resources.getString(R.string.notification_error_message_daemon_exited, exception.cause!!.defaultMessage()), exception)
      } else {
        log.info("tinc daemon started.")
        broadcastEvent(Actions.EVENT_CONNECTED)
      }

      if (interfaceCfg.reconnectOnNetworkChange)
        connectivityChangeReceiver.registerWatcher(this)
    }
  }

  private fun stopVpn(): CompletableFuture<Unit> = synchronized(this) {
    log.info("Stopping any running tinc daemon.")

    connectivityChangeReceiver.unregisterWatcher(this)

    getCurrentNetName()?.let {
      Tinc.stop(it).handle { _, _ ->
        log.info("All tinc daemons stopped.")
        broadcastEvent(Actions.EVENT_DISCONNECTED)
        setState(null, null, null, null, null)
      }
    } ?: CompletableFuture.completedFuture(Unit)
  }

  private fun reportError(msg: String, e: Throwable? = null, docTopic: String? = null) {
    if (e != null)
      log.error(msg, e)
    else
      log.error(msg)

    broadcastEvent(Actions.EVENT_ABORTED)
    App.alert(R.string.notification_error_title_unable_to_start_tinc, msg,
      if (docTopic != null) resources.getString(R.string.app_doc_url_format, docTopic) else null)
  }

  private fun broadcastEvent(event: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(event))
  }

  private fun serveDeviceFd(serverSocket: LocalServerSocket, deviceFd: ParcelFileDescriptor) =
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

  private fun waitForDaemonStartup() =
    Executor
      .runAsyncTask { Thread.sleep(SETUP_DELAY) }
      .thenCompose { if (daemon!!.isDone) daemon!! else Executor.runAsyncTask { Unit } }

  companion object {
    private const val SETUP_DELAY = 500L // ms
    private const val DEVICE_FD_ABSTRACT_SOCKET = "${BuildConfig.APPLICATION_ID}.daemon.socket"

    private val STORE_NAME = this::class.java.`package`!!.name
    private const val STORE_KEY_NETNAME = "netname"
    private const val STORE_KEY_PASSPHRASE = "passphrase"

    private val context by lazy { App.getContext() }
    private val store by lazy { context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)!! }

    private var interfaceCfg: VpnInterfaceConfiguration? = null
    private var fd: ParcelFileDescriptor? = null
    private var daemon: CompletableFuture<Unit>? = null

    private fun saveConnection(netName: String?, passphrase: String?) =
      store.edit()
        .putString(STORE_KEY_NETNAME, netName)
        .putString(STORE_KEY_PASSPHRASE, passphrase)
        .apply()

    private fun setState(netName: String?, passphrase: String?, interfaceCfg: VpnInterfaceConfiguration?,
                         fd: ParcelFileDescriptor?, daemon: CompletableFuture<Unit>?) {
      saveConnection(netName, passphrase)
      TincVpnService.interfaceCfg = interfaceCfg
      TincVpnService.fd = fd
      TincVpnService.daemon = daemon
    }

    private fun getPassphrase(): String? = store.getString(STORE_KEY_PASSPHRASE, null)
    fun getCurrentNetName(): String? = store.getString(STORE_KEY_NETNAME, null)

    fun getCurrentInterfaceCfg() = interfaceCfg
    fun isConnected() = !(daemon?.isDone ?: true)

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
