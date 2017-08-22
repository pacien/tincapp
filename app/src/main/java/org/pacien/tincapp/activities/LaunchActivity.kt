package org.pacien.tincapp.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import org.pacien.tincapp.context.App
import org.pacien.tincapp.intent.action.ACTION_CONNECT
import org.pacien.tincapp.intent.action.ACTION_DISCONNECT
import org.pacien.tincapp.intent.action.TINC_SCHEME
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class LaunchActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.action) {
            ACTION_CONNECT -> requestPerm()
            ACTION_DISCONNECT -> disconnect()
        }
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == Activity.RESULT_OK) TincVpnService.startVpn(intent.data.schemeSpecificPart)
        finish()
    }

    private fun requestPerm() = VpnService.prepare(this).let {
        if (it != null)
            startActivityForResult(it, 0)
        else
            onActivityResult(0, Activity.RESULT_OK, null)
    }

    private fun disconnect() {
        TincVpnService.stopVpn()
        finish()
    }

    companion object {

        fun connect(netName: String) {
            App.getContext().startActivity(Intent(App.getContext(), LaunchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setAction(ACTION_CONNECT)
                    .setData(Uri.Builder().scheme(TINC_SCHEME).opaquePart(netName).build()))
        }

        fun disconnect() {
            App.getContext().startActivity(Intent(App.getContext(), LaunchActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setAction(ACTION_DISCONNECT))
        }

    }

}
