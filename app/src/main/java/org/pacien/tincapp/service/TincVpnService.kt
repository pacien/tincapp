package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.ParcelFileDescriptor
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import org.pacien.tincapp.extensions.VpnServiceBuilder.applyCfg
import org.pacien.tincapp.intent.action.ACTION_START_SERVICE
import org.pacien.tincapp.intent.action.ACTION_STOP_SERVICE
import org.pacien.tincapp.intent.action.TINC_SCHEME
import java.io.IOException

/**
 * @author pacien
 */
class TincVpnService : VpnService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.action) {
            ACTION_START_SERVICE -> startVpn(intent.data.schemeSpecificPart)
            ACTION_STOP_SERVICE -> onDestroy()
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        connected = false

        try {
            if (netName != null) Tinc.stop(netName!!)
            fd?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            netName = null
            interfaceCfg = null
            fd = null
            super.onDestroy()
        }
    }

    private fun startVpn(netName: String) {
        if (isConnected()) onDestroy()
        TincVpnService.netName = netName
        TincVpnService.interfaceCfg = VpnInterfaceConfiguration.fromIfaceConfiguration(AppPaths.netConfFile(netName))

        val net = Builder().setSession(netName).applyCfg(TincVpnService.interfaceCfg!!)
        applyIgnoringException(net::addDisallowedApplication, BuildConfig.APPLICATION_ID)

        try {
            fd = net.establish()
            Tincd.start(netName, fd!!.fd)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        connected = true
    }

    companion object {

        private var connected: Boolean = false
        private var netName: String? = null
        private var interfaceCfg: VpnInterfaceConfiguration? = null
        private var fd: ParcelFileDescriptor? = null

        fun startVpn(netName: String) {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .setAction(ACTION_START_SERVICE)
                    .setData(Uri.Builder().scheme(TINC_SCHEME).opaquePart(netName).build()))
        }

        fun stopVpn() {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .setAction(ACTION_STOP_SERVICE))
        }

        fun getCurrentNetName() = netName
        fun getCurrentInterfaceCfg() = interfaceCfg
        fun isConnected() = connected

    }

}
