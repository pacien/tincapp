/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2019 Euxane P. TRAN-GIRARD
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
import androidx.appcompat.app.AlertDialog
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.activities.common.ProgressModal
import org.pacien.tincapp.activities.common.RecentCrashHandler
import org.pacien.tincapp.activities.start.StartActivity
import org.pacien.tincapp.activities.viewlog.ViewLogActivity
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.intent.BroadcastMapper
import org.pacien.tincapp.service.TincVpnService
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import org.pacien.tincapp.activities.status.nodes.NodeListViewModel
import org.pacien.tincapp.activities.status.subnets.SubnetListViewModel
import org.pacien.tincapp.activities.status.ui.NodeInfoDialog
import org.pacien.tincapp.activities.status.ui.StatusScreen
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.ui.theme.TincTheme

/**
 * @author euxane
 */
class StatusActivity : BaseActivity() {
  private val recentCrashHandler by lazy { RecentCrashHandler(this) }
  private val vpnService by lazy { TincVpnService }
  private val netName by lazy { vpnService.getCurrentNetName() }
  private val broadcastMapper = BroadcastMapper(mapOf(Actions.EVENT_DISCONNECTED to this::onVpnShutdown))
  private val nodeListViewModel by lazy { ViewModelProvider(this)[NodeListViewModel::class.java] }
  private val subnetListViewModel by lazy { ViewModelProvider(this)[SubnetListViewModel::class.java] }

  private var shutdownDialog: AlertDialog? = null
  private var listNetworksAfterExit = true

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      TincTheme {
        val vpnCfg = vpnService.getCurrentInterfaceCfg() ?: org.pacien.tincapp.data.VpnInterfaceConfiguration()
        val nodes by nodeListViewModel.nodeList.observeAsState(emptyList())
        val subnets by subnetListViewModel.nodeList.observeAsState(emptyList())

        var selectedNodeInfo by remember { mutableStateOf<String?>(null) }

        StatusScreen(
          vpnInterfaceConfiguration = vpnCfg,
          nodes = nodes,
          subnets = subnets,
          onNodeClick = { node ->
            Tinc.info(netName!!, node.name).thenAccept { info ->
              runOnUiThread { selectedNodeInfo = info }
            }
          },
          netName = netName,
          onDisconnectClick = { stopVpn() },
          onShowLogClick = { openLogViewer() },
          onAboutClick = { showAboutDialog() }
        )

        selectedNodeInfo?.let { info ->
          NodeInfoDialog(
            nodeInfoText = info,
            onDismiss = { selectedNodeInfo = null }
          )
        }
      }
    }

    handleStartIntentAction(savedInstanceState)
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

  private fun openLogViewer() =
    startActivity(Intent(this, ViewLogActivity::class.java))

  private fun handleStartIntentAction(savedInstanceState: Bundle?) {
    val disconnectRequested = intent.action == Actions.ACTION_DISCONNECT && !isLaunchedFromHistory()
    listNetworksAfterExit = !disconnectRequested
    if (disconnectRequested && savedInstanceState == null) stopVpn()
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
