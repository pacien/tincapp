package org.pacien.tincapp.activities

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import org.pacien.tincapp.context.App
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class PromptActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent.getSerializableExtra(INTENT_EXTRA_ACTION) as Action) {
            Action.REQUEST_PERMISSION -> requestVpnPermission()
        }
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == Activity.RESULT_OK) TincVpnService.startVpn(intent.getStringExtra(INTENT_EXTRA_NET_NAME))
        finish()
    }

    private fun requestVpnPermission() = VpnService.prepare(this).let {
        if (it != null)
            startActivityForResult(it, 0)
        else
            onActivityResult(0, Activity.RESULT_OK, Intent())
    }

    companion object {
        private val INTENT_EXTRA_ACTION = "action"
        private val INTENT_EXTRA_NET_NAME = "netName"

        private enum class Action { REQUEST_PERMISSION }

        fun requestVpnPermission(netName: String) {
            App.getContext().startActivity(Intent(App.getContext(), PromptActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(INTENT_EXTRA_ACTION, Action.REQUEST_PERMISSION)
                    .putExtra(INTENT_EXTRA_NET_NAME, netName))
        }
    }

}
