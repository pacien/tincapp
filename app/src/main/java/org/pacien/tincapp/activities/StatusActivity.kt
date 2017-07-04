package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.dialog_text_monopsace.view.*
import kotlinx.android.synthetic.main.fragment_network_status_header.*
import kotlinx.android.synthetic.main.page_status.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.service.VpnInterfaceConfiguration

/**
 * @author pacien
 */
class StatusActivity : BaseActivity(), AdapterView.OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.page_status, main_content)
        writeContent()
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_status, m)
        return super.onCreateOptionsMenu(m)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val nodeName = (view as TextView).text.toString()
        val v = layoutInflater.inflate(R.layout.dialog_text_monopsace, main_content, false)
        v.dialog_text_monospace.text = Tinc.info(TincVpnService.getCurrentNetName()!!, nodeName)

        AlertDialog.Builder(this)
                .setTitle(R.string.title_node_info)
                .setView(v)
                .setPositiveButton(R.string.action_close) { _, _ -> /* nop */ }
                .show()
    }

    fun stopVpn(@Suppress("UNUSED_PARAMETER") i: MenuItem) {
        TincVpnService.stopVpn()
        startActivity(Intent(this, StartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }

    private fun TextView.setText(list: List<String>) {
        if (list.isNotEmpty()) text = list.joinToString("\n")
        else text = getString(R.string.value_none)
    }

    private fun getNodeNames() = Tinc.dumpNodes(TincVpnService.getCurrentNetName()!!).map { it.substringBefore(" ") }

    private fun writeContent() {
        node_list.addHeaderView(layoutInflater.inflate(R.layout.fragment_network_status_header, node_list, false), null, false)
        node_list.addFooterView(View(this), null, false)
        node_list.emptyView = node_list_empty
        node_list.onItemClickListener = this
        node_list.adapter = ArrayAdapter<String>(this, R.layout.fragment_list_item, getNodeNames())

        text_network_name.text = TincVpnService.getCurrentNetName() ?: getString(R.string.value_none)
        writeNetworkInfo(TincVpnService.getCurrentInterfaceCfg() ?: VpnInterfaceConfiguration())
    }


    private fun writeNetworkInfo(cfg: VpnInterfaceConfiguration) {
        text_network_ip_addresses.setText(cfg.addresses.map { it.toString() })
        text_network_routes.setText(cfg.routes.map { it.toString() })
        text_network_dns_servers.setText(cfg.dnsServers)
        text_network_search_domains.setText(cfg.searchDomains)
        text_network_allow_bypass.text = getString(if (cfg.allowBypass) R.string.value_yes else R.string.value_no)

        block_network_allowed_applications.visibility = if (cfg.allowedApplications.isNotEmpty()) View.VISIBLE else View.GONE
        text_network_allowed_applications.setText(cfg.allowedApplications)

        block_network_disallowed_applications.visibility = if (cfg.disallowedApplications.isNotEmpty()) View.VISIBLE else View.GONE
        text_network_disallowed_applications.setText(cfg.disallowedApplications)
    }

}
