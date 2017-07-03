package org.pacien.tincapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.base.*

import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.page_start, main_content)
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_start, m)
        return super.onCreateOptionsMenu(m)
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        notify(if (result == Activity.RESULT_OK) R.string.message_vpn_permissions_granted else R.string.message_vpn_permissions_denied)
    }

    fun requestVpnPermission(@Suppress("UNUSED_PARAMETER") v: View) {
        val askPermIntent = VpnService.prepare(this)

        if (askPermIntent != null)
            startActivityForResult(askPermIntent, 0)
        else
            onActivityResult(0, Activity.RESULT_OK, null)
    }

    fun startVpnDialog(@Suppress("UNUSED_PARAMETER") v: View) {
        val i = EditText(this)
        i.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        i.setHint(R.string.field_net_name)

        @SuppressLint("InflateParams")
        val vg = layoutInflater.inflate(R.layout.dialog_frame, main_content, false) as ViewGroup
        vg.addView(i)

        AlertDialog.Builder(this)
                .setTitle(R.string.title_connect_to_network)
                .setView(vg)
                .setPositiveButton(R.string.action_connect) { _, _ -> startVpn(i.text.toString()) }
                .setNegativeButton(R.string.action_close) { _, _ -> /* nop */ }
                .show()
    }

    fun openConfigureActivity(@Suppress("UNUSED_PARAMETER") i: MenuItem) = startActivity(Intent(this, ConfigureActivity::class.java))

    private fun startVpn(netName: String) {
        startService(Intent(this, TincVpnService::class.java).putExtra(TincVpnService.INTENT_EXTRA_NET_CONF,
                if (netName.startsWith("external/")) AppPaths.NetConf(AppPaths.Storage.EXTERNAL, netName.substringAfter("/"))
                else AppPaths.NetConf(AppPaths.Storage.INTERNAL, netName)))
    }

}
