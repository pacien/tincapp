package org.pacien.tincapp.service

import android.app.Service
import android.content.Intent
import android.net.VpnService
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.Tincd
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.utils.applyIgnoringException
import java.io.IOException

/**
 * @author pacien
 */
class TincVpnService : VpnService() {

    private var netConf: AppPaths.NetConf? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        netConf = intent.getSerializableExtra(INTENT_EXTRA_NET_CONF)!! as AppPaths.NetConf

        val net = Builder().setSession(netConf!!.netName)
        net.apply(VpnInterfaceConfiguration(AppPaths.netConfFile(netConf!!)))
        applyIgnoringException(net::addDisallowedApplication, BuildConfig.APPLICATION_ID)

        try {
            Tincd.start(netConf!!, net.establish().detachFd())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Service.START_STICKY
    }

    override fun onDestroy() {
        try {
            Tinc.stop(netConf!!)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        val INTENT_EXTRA_NET_CONF = "netConf"
    }

}
