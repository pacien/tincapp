package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.utils.applyIgnoringException
import java.io.IOException


/**
 * @author pacien
 */
class TincVpnService : VpnService() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        when (intent.getSerializableExtra(INTENT_EXTRA_ACTION)) {
            Action.START -> startVpn(intent.getStringExtra(INTENT_EXTRA_NET_NAME)!!)
            Action.STOP -> onDestroy()
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
        TincVpnService.interfaceCfg = VpnInterfaceConfiguration(AppPaths.netConfFile(netName))

        val net = Builder().setSession(netName).apply(TincVpnService.interfaceCfg!!)
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

        private val INTENT_EXTRA_ACTION = "action"
        private val INTENT_EXTRA_NET_NAME = "netName"

        private enum class Action { START, STOP }

        private var connected: Boolean = false
        private var netName: String? = null
        private var interfaceCfg: VpnInterfaceConfiguration? = null
        private var fd: ParcelFileDescriptor? = null

        fun startVpn(netName: String) {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .putExtra(INTENT_EXTRA_ACTION, Action.START)
                    .putExtra(TincVpnService.INTENT_EXTRA_NET_NAME, netName))
        }

        fun stopVpn() {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .putExtra(INTENT_EXTRA_ACTION, Action.STOP))
        }

        fun getCurrentNetName() = netName
        fun getCurrentInterfaceCfg() = interfaceCfg
        fun isConnected() = connected

    }

}
