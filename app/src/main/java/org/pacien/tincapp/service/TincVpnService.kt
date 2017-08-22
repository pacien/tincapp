package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import org.apache.commons.configuration2.ex.ConversionException
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import org.pacien.tincapp.extensions.VpnServiceBuilder.applyCfg
import org.pacien.tincapp.intent.action.TINC_SCHEME
import java.io.FileNotFoundException
import java.io.IOException

/**
 * @author pacien
 */
class TincVpnService : VpnService() {

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (isConnected()) stopVpn()
        startVpn(intent.data.schemeSpecificPart)
        return Service.START_REDELIVER_INTENT
    }

    private fun startVpn(netName: String) {
        if (netName.isBlank())
            return reportError(resources.getString(R.string.message_no_network_name_provided), docTopic = "intent-api")

        if (!AppPaths.confDir(netName).exists())
            return reportError(resources.getString(R.string.message_no_configuration_for_network_format, netName), docTopic = "configuration")

        Log.i(TAG, "Starting tinc daemon for network \"$netName\".")

        val interfaceCfg = try {
            VpnInterfaceConfiguration.fromIfaceConfiguration(AppPaths.existing(AppPaths.netConfFile(netName)))
        } catch (e: FileNotFoundException) {
            return reportError(resources.getString(R.string.message_network_config_not_found_format, e.message!!), e, "configuration")
        } catch (e: ConversionException) {
            return reportError(resources.getString(R.string.message_network_config_invalid_format, e.message!!), e, "network-interface")
        }

        val fd = try {
            Builder().setSession(netName)
                    .applyCfg(interfaceCfg)
                    .also { applyIgnoringException(it::addDisallowedApplication, BuildConfig.APPLICATION_ID) }
                    .establish()
        } catch (e: IllegalArgumentException) {
            return reportError(resources.getString(R.string.message_network_config_invalid_format, e.message!!), e, "network-interface")
        }

        Tincd.start(netName, fd!!.fd)
        setState(true, netName, interfaceCfg, fd)
        Log.i(TAG, "tinc daemon started.")
    }

    private fun reportError(msg: String, e: Throwable? = null, docTopic: String? = null) {
        if (e != null)
            Log.e(TAG, msg, e)
        else
            Log.e(TAG, msg)

        App.alert(R.string.title_unable_to_start_tinc, msg,
                if (docTopic != null) resources.getString(R.string.app_doc_url_format, docTopic) else null)
    }

    companion object {

        val TAG = this::class.java.canonicalName!!

        private var connected: Boolean = false
        private var netName: String? = null
        private var interfaceCfg: VpnInterfaceConfiguration? = null
        private var fd: ParcelFileDescriptor? = null

        private fun setState(connected: Boolean, netName: String?, interfaceCfg: VpnInterfaceConfiguration?, fd: ParcelFileDescriptor?) {
            TincVpnService.connected = connected
            TincVpnService.netName = netName
            TincVpnService.interfaceCfg = interfaceCfg
            TincVpnService.fd = fd
        }

        fun startVpn(netName: String) {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .setData(Uri.Builder().scheme(TINC_SCHEME).opaquePart(netName).build()))
        }

        fun stopVpn() {
            try {
                Log.i(TAG, "Stopping any running tinc daemon.")
                if (netName != null) Tinc.stop(netName!!)
                fd?.close()
                Log.i(TAG, "All tinc daemons stopped.")
            } catch (e: IOException) {
                Log.wtf(TAG, e)
            } finally {
                setState(false, null, null, null)
            }
        }

        fun getCurrentNetName() = netName
        fun getCurrentInterfaceCfg() = interfaceCfg
        fun isConnected() = connected

    }

}
