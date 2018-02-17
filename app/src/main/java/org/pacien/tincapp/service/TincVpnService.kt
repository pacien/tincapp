package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import java8.util.concurrent.CompletableFuture
import org.apache.commons.configuration2.ex.ConversionException
import org.bouncycastle.openssl.PEMException
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.TincConfiguration
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import org.pacien.tincapp.extensions.VpnServiceBuilder.applyCfg
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.utils.TincKeyring
import java.io.FileNotFoundException

/**
 * @author pacien
 */
class TincVpnService : VpnService() {
  override fun onDestroy() {
    stopVpn()
    super.onDestroy()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    Log.i(TAG, intent.action)

    when {
      intent.action == Actions.ACTION_CONNECT && intent.scheme == Actions.TINC_SCHEME ->
        startVpn(intent.data.schemeSpecificPart, intent.data.fragment)
      intent.action == Actions.ACTION_DISCONNECT ->
        stopVpn()
      else ->
        throw IllegalArgumentException("Invalid intent action received.")
    }

    return Service.START_NOT_STICKY
  }

  private fun startVpn(netName: String, passphrase: String? = null): Unit = synchronized(this) {
    if (netName.isBlank())
      return reportError(resources.getString(R.string.message_no_network_name_provided), docTopic = "intent-api")

    if (TincKeyring.needsPassphrase(netName) && passphrase == null)
      return reportError(resources.getString(R.string.message_passphrase_required))

    if (!AppPaths.storageAvailable())
      return reportError(resources.getString(R.string.message_storage_unavailable))

    if (!AppPaths.confDir(netName).exists())
      return reportError(resources.getString(R.string.message_no_configuration_for_network_format, netName), docTopic = "configuration")

    Log.i(TAG, "Starting tinc daemon for network \"$netName\".")
    if (isConnected()) stopVpn()

    val interfaceCfg = try {
      VpnInterfaceConfiguration.fromIfaceConfiguration(AppPaths.existing(AppPaths.netConfFile(netName)))
    } catch (e: FileNotFoundException) {
      return reportError(resources.getString(R.string.message_network_config_not_found_format, e.message!!), e, "configuration")
    } catch (e: ConversionException) {
      return reportError(resources.getString(R.string.message_network_config_invalid_format, e.message!!), e, "network-interface")
    }

    val deviceFd = try {
      Builder().setSession(netName)
        .applyCfg(interfaceCfg)
        .also { applyIgnoringException(it::addDisallowedApplication, BuildConfig.APPLICATION_ID) }
        .establish()!!
    } catch (e: IllegalArgumentException) {
      return reportError(resources.getString(R.string.message_network_config_invalid_format, e.message!!), e, "network-interface")
    }

    val privateKeys = try {
      TincConfiguration.fromTincConfiguration(AppPaths.existing(AppPaths.tincConfFile(netName))).let { tincCfg ->
        Pair(
          TincKeyring.openPrivateKey(tincCfg.ed25519PrivateKeyFile ?: AppPaths.defaultEd25519PrivateKeyFile(netName), passphrase),
          TincKeyring.openPrivateKey(tincCfg.privateKeyFile ?: AppPaths.defaultRsaPrivateKeyFile(netName), passphrase))
      }
    } catch (e: FileNotFoundException) {
      Pair(null, null)
    } catch (e: PEMException) {
      return reportError(resources.getString(R.string.message_could_not_decrypt_private_keys_format, e.message))
    }

    val daemon = Tincd.start(netName, deviceFd.fd, privateKeys.first?.fd, privateKeys.second?.fd)
    setState(netName, interfaceCfg, deviceFd, daemon)
    waitForDaemonStartup().thenRun {
      deviceFd.close()
      privateKeys.first?.close()
      privateKeys.second?.close()
      Log.i(TAG, "tinc daemon started.")
      broadcastEvent(Actions.EVENT_CONNECTED)
    }
  }

  private fun stopVpn(): Unit = synchronized(this) {
    Log.i(TAG, "Stopping any running tinc daemon.")
    netName?.let {
      Tinc.stop(it).thenRun {
        Log.i(TAG, "All tinc daemons stopped.")
        broadcastEvent(Actions.EVENT_DISCONNECTED)
        setState(null, null, null, null)
      }
    }
  }

  private fun reportError(msg: String, e: Throwable? = null, docTopic: String? = null) {
    if (e != null)
      Log.e(TAG, msg, e)
    else
      Log.e(TAG, msg)

    broadcastEvent(Actions.EVENT_ABORTED)
    App.alert(R.string.title_unable_to_start_tinc, msg,
      if (docTopic != null) resources.getString(R.string.app_doc_url_format, docTopic) else null)
  }

  private fun broadcastEvent(event: String) {
    LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(event))
  }

  private fun waitForDaemonStartup() =
    CompletableFuture
      .runAsync { Thread.sleep(SETUP_DELAY) }
      .thenCompose { netName?.let { Tinc.pid(it) } ?: CompletableFuture.completedFuture(0) }

  companion object {
    private const val SETUP_DELAY = 500L // ms
    private val TAG = this::class.java.canonicalName!!
    private var netName: String? = null
    private var interfaceCfg: VpnInterfaceConfiguration? = null
    private var fd: ParcelFileDescriptor? = null
    private var daemon: CompletableFuture<Void>? = null

    private fun setState(netName: String?, interfaceCfg: VpnInterfaceConfiguration?,
                         fd: ParcelFileDescriptor?, daemon: CompletableFuture<Void>?) {

      TincVpnService.netName = netName
      TincVpnService.interfaceCfg = interfaceCfg
      TincVpnService.fd = fd
      TincVpnService.daemon = daemon
    }

    fun getCurrentNetName() = netName
    fun getCurrentInterfaceCfg() = interfaceCfg
    fun isConnected() = !(daemon?.isDone ?: true)

    fun connect(netName: String, passphrase: String? = null) {
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
