package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
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

    private var netName: String? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startVpn(intent.getStringExtra(INTENT_EXTRA_NET_NAME)!!)
        return Service.START_STICKY
    }

    override fun onDestroy() = try {
        Tinc.stop(netName!!)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        netName = null
    }

    private fun startVpn(netName: String) {
        if (netName == this.netName) onDestroy()
        this.netName = netName

        val net = Builder().setSession(netName)
        net.apply(VpnInterfaceConfiguration(AppPaths.netConfFile(netName)))
        applyIgnoringException(net::addDisallowedApplication, BuildConfig.APPLICATION_ID)

        try {
            Tincd.start(netName, net.establish().detachFd())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    companion object {
        private val INTENT_EXTRA_NET_NAME = "netName"

        fun startVpn(netName: String) {
            App.getContext().startService(Intent(App.getContext(), TincVpnService::class.java)
                    .putExtra(TincVpnService.INTENT_EXTRA_NET_NAME, netName))
        }
    }

}
