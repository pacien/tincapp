/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2019 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.start_activity.*
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.activities.common.ProgressModal
import org.pacien.tincapp.activities.common.RecentCrashHandler
import org.pacien.tincapp.activities.configure.ConfigureActivity
import org.pacien.tincapp.activities.status.StatusActivity
import org.pacien.tincapp.context.App
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.intent.BroadcastMapper
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class StartActivity : BaseActivity() {
  val permissionRequestCode = 0
  private val connectionStarter by lazy { ConnectionStarter(this) }
  private val recentCrashHandler by lazy { RecentCrashHandler(this) }
  private val broadcastMapper = BroadcastMapper(mapOf(
    Actions.EVENT_CONNECTED to this::onVpnStart,
    Actions.EVENT_ABORTED to this::onVpnStartError
  ))

  var connectDialog: AlertDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.start_activity)
    initNetworkListFragment()

    if (intent.action == Actions.ACTION_CONNECT && intent.data?.schemeSpecificPart != null)
      connectionStarter.tryStart(intent.data.schemeSpecificPart, intent.data.fragment, false)
  }

  private fun initNetworkListFragment() {
    val fragment = start_activity_network_list_fragment as NetworkListFragment
    fragment.connectToNetworkAction = { netName -> connectionStarter.tryStart(netName, displayStatus = true) }
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_start, m)
    return super.onCreateOptionsMenu(m)
  }

  override fun onDestroy() {
    connectDialog?.dismiss()
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    if (TincVpnService.isConnected()) openStatusActivity(false)
    broadcastMapper.register()
    recentCrashHandler.handleRecentCrash()
  }

  override fun onPause() {
    broadcastMapper.unregister()
    super.onPause()
  }

  override fun onActivityResult(request: Int, result: Int, data: Intent?): Unit = when (request) {
    permissionRequestCode -> continueConnectionStart(result)
    else -> throw IllegalArgumentException("Result for unknown request received.")
  }

  private fun continueConnectionStart(result: Int): Unit = when (result) {
    Activity.RESULT_OK -> connectionStarter.tryStart()
    else -> App.alert(R.string.notification_error_title_unable_to_start_tinc, getString(R.string.notification_error_message_could_not_bind_iface))
  }

  private fun onVpnStart() {
    connectDialog?.dismiss()
    if (connectionStarter.displayStatus()) openStatusActivity()
    finish()
  }

  private fun onVpnStartError() {
    connectDialog?.dismiss()
  }

  fun showConnectProgressDialog() {
    connectDialog = ProgressModal.show(this, resources.getString(R.string.start_starting_vpn))
  }

  @Suppress("UNUSED_PARAMETER")
  fun openConfigureActivity(m: MenuItem) =
    startActivity(Intent(this, ConfigureActivity::class.java))

  private fun openStatusActivity(transition: Boolean = true) {
    val intent = Intent(this, StatusActivity::class.java)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .apply { if (!transition) addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) }

    startActivity(intent)
  }
}
