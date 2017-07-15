package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import java8.util.concurrent.CompletableFuture
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.dialog_text_monopsace.view.*
import kotlinx.android.synthetic.main.fragment_list_view.*
import kotlinx.android.synthetic.main.fragment_network_status_header.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Android.setElements
import org.pacien.tincapp.extensions.Android.setText
import org.pacien.tincapp.service.TincVpnService
import java.util.*
import kotlin.concurrent.timerTask

/**
 * @author pacien
 */
class StatusActivity : BaseActivity(), AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var nodeListAdapter: ArrayAdapter<String>? = null
    private var refreshTimer: Timer? = null
    private var updateView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nodeListAdapter = ArrayAdapter<String>(this, R.layout.fragment_list_item)
        refreshTimer = Timer(true)

        layoutInflater.inflate(R.layout.fragment_list_view, main_content)
        list_wrapper.setOnRefreshListener(this)
        list.addHeaderView(layoutInflater.inflate(R.layout.fragment_network_status_header, list, false), null, false)
        list.addFooterView(View(this), null, false)
        list.onItemClickListener = this
        list.adapter = nodeListAdapter
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_status, m)
        return super.onCreateOptionsMenu(m)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshTimer?.cancel()
        nodeListAdapter = null
        refreshTimer = null
    }

    override fun onStart() {
        super.onStart()
        writeNetworkInfo(TincVpnService.getCurrentInterfaceCfg() ?: VpnInterfaceConfiguration())
        updateView = true
        onRefresh()
        updateNodeList()
    }

    override fun onStop() {
        super.onStop()
        updateView = false
    }

    override fun onResume() {
        super.onResume()
        if (!TincVpnService.isConnected()) openStartActivity()
    }

    override fun onRefresh() {
        getNodeNames().thenAccept {
            runOnUiThread {
                nodeListAdapter?.setElements(it)
                node_list_placeholder.visibility = if (nodeListAdapter?.isEmpty ?: true) View.VISIBLE else View.GONE
                list_wrapper.isRefreshing = false
                if (!TincVpnService.isConnected()) openStartActivity()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val nodeName = (view as TextView).text.toString()
        val dialogTextView = layoutInflater.inflate(R.layout.dialog_text_monopsace, main_content, false)
        Tinc.info(TincVpnService.getCurrentNetName()!!, nodeName).thenAccept {
            runOnUiThread {
                dialogTextView.dialog_text_monospace.text = it
                AlertDialog.Builder(this)
                        .setTitle(R.string.title_node_info)
                        .setView(dialogTextView)
                        .setPositiveButton(R.string.action_close) { _, _ -> /* nop */ }
                        .show()
            }
        }
    }

    fun writeNetworkInfo(cfg: VpnInterfaceConfiguration) {
        text_network_name.text = TincVpnService.getCurrentNetName() ?: getString(R.string.value_none)
        text_network_ip_addresses.setText(cfg.addresses.map { it.toSlashSeparated() })
        text_network_routes.setText(cfg.routes.map { it.toSlashSeparated() })
        text_network_dns_servers.setText(cfg.dnsServers)
        text_network_search_domains.setText(cfg.searchDomains)
        text_network_allow_bypass.text = getString(if (cfg.allowBypass) R.string.value_yes else R.string.value_no)
        block_network_allowed_applications.visibility = if (cfg.allowedApplications.isNotEmpty()) View.VISIBLE else View.GONE
        text_network_allowed_applications.setText(cfg.allowedApplications)
        block_network_disallowed_applications.visibility = if (cfg.disallowedApplications.isNotEmpty()) View.VISIBLE else View.GONE
        text_network_disallowed_applications.setText(cfg.disallowedApplications)
    }

    fun updateNodeList() {
        refreshTimer?.schedule(timerTask {
            onRefresh()
            if (updateView) updateNodeList()
        }, REFRESH_RATE)
    }

    fun stopVpn(@Suppress("UNUSED_PARAMETER") i: MenuItem) {
        TincVpnService.stopVpn()
        openStartActivity()
        finish()
    }

    fun openStartActivity() = startActivity(Intent(this, StartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))

    companion object {
        private val REFRESH_RATE = 5000L

        fun getNodeNames(): CompletableFuture<List<String>> = when (TincVpnService.isConnected()) {
            true -> Tinc.dumpNodes(TincVpnService.getCurrentNetName()!!).thenApply<List<String>> { it.map { it.substringBefore(' ') } }
            false -> CompletableFuture.supplyAsync<List<String>> { emptyList() }
        }
    }

}
