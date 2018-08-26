/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.activities.status

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.status_activity.*
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.activities.common.FragmentListPagerAdapter
import org.pacien.tincapp.activities.common.ProgressModal
import org.pacien.tincapp.activities.common.RecentCrashHandler
import org.pacien.tincapp.activities.start.StartActivity
import org.pacien.tincapp.activities.status.networkinfo.NetworkInfoFragment
import org.pacien.tincapp.activities.status.nodes.NodeListFragment
import org.pacien.tincapp.activities.status.subnets.SubnetListFragment
import org.pacien.tincapp.activities.viewlog.ViewLogActivity
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.intent.BroadcastMapper
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class StatusActivity : BaseActivity() {
  private val recentCrashHandler by lazy { RecentCrashHandler(this) }
  private val vpnService by lazy { TincVpnService }
  private val netName by lazy { vpnService.getCurrentNetName() }
  private val pagerAdapter by lazy { FragmentListPagerAdapter(pages, supportFragmentManager) }
  private val broadcastMapper = BroadcastMapper(mapOf(Actions.EVENT_DISCONNECTED to this::onVpnShutdown))
  private val pages = listOf(
    R.string.status_activity_title_network_info to NetworkInfoFragment(),
    R.string.status_activity_title_node_list to NodeListFragment(),
    R.string.status_activity_title_subnet_list to SubnetListFragment()
  )

  private var shutdownDialog: AlertDialog? = null
  private var listNetworksAfterExit = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.status_activity)
    status_activity_pager.adapter = pagerAdapter
    supportActionBar.subtitle = getString(R.string.status_activity_state_connected_to_format, netName)
    handleStartIntentAction(intent.action)
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_status, m)
    return super.onCreateOptionsMenu(m)
  }

  override fun onResume() {
    super.onResume()
    if (!TincVpnService.isConnected()) openStartActivity()

    broadcastMapper.register()
    recentCrashHandler.handleRecentCrash()
  }

  override fun onPause() {
    broadcastMapper.unregister()
    super.onPause()
  }

  private fun onVpnShutdown() {
    shutdownDialog?.dismiss()
    if (listNetworksAfterExit) openStartActivity()
    finish()
  }

  @Suppress("UNUSED_PARAMETER")
  fun stopVpn(m: MenuItem) =
    stopVpn()

  @Suppress("UNUSED_PARAMETER")
  fun openLogViewer(m: MenuItem) =
    startActivity(Intent(this, ViewLogActivity::class.java))

  private fun handleStartIntentAction(intentAction: String?) = when (intentAction) {
    Actions.ACTION_DISCONNECT -> {
      listNetworksAfterExit = false
      stopVpn()
    }

    else -> listNetworksAfterExit = true
  }

  private fun stopVpn() {
    shutdownDialog = ProgressModal.show(this, getString(R.string.status_activity_disconnecting_vpn))
    vpnService.disconnect()
  }

  private fun openStartActivity() {
    startActivity(Intent(this, StartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    finish()
  }
}
