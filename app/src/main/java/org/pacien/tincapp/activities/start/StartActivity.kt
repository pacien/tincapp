/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.storageprovider.FilesDocumentsProvider
import org.pacien.tincapp.ui.theme.TincTheme

/**
 * @author euxane
 */
class StartActivity : BaseActivity() {
  val permissionRequestCode = 0
  private val connectionStarter by lazy { ConnectionStarter(this) }
  private val recentCrashHandler by lazy { RecentCrashHandler(this) }
  private val broadcastMapper = BroadcastMapper(mapOf(
    Actions.EVENT_CONNECTED to this::onVpnStart,
    Actions.EVENT_ABORTED to this::onVpnStartError
  ))

  private val viewModel: StartViewModel by viewModels()

  private var connectDialog: androidx.appcompat.app.AlertDialog? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    setContent {
      TincTheme {
        StartScreen(
          viewModel = viewModel,
          onConfigureClick = { openConfigureActivity() },
          onAboutClick = { showAboutDialog() },
          onNetworkClick = { connectToNetworkAction(it) },
          onManualClick = { App.openURL(it) },
          onConfigDirClick = { openDocumentTree(BrowseFilesIntents.networkConfigDirDocId(it)) },
          onLogDirClick = { openDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) }
        )
      }
    }

    if (isFirstIntentDelivery(savedInstanceState) && intent.action == Actions.ACTION_CONNECT && intent.data?.schemeSpecificPart != null)
      connectionStarter.tryStart(intent.data!!.schemeSpecificPart, intent.data!!.fragment, false)
  }

  override fun onResume() {
    super.onResume()
    if (TincVpnService.isConnected()) openStatusActivity(false)
    broadcastMapper.register()
    recentCrashHandler.handleRecentCrash()
    viewModel.refresh()
  }

  override fun onPause() {
    broadcastMapper.unregister()
    super.onPause()
  }

  override fun onDestroy() {
    connectDialog?.dismiss()
    super.onDestroy()
  }

  override fun onActivityResult(request: Int, result: Int, data: Intent?) {
    when (request) {
      permissionRequestCode -> continueConnectionStart(result)
      else -> throw IllegalArgumentException("Result for unknown request received.")
    }

    super.onActivityResult(request, result, data)
  }

  private fun connectToNetworkAction(netName: String) {
    connectionStarter.tryStart(netName, displayStatus = true)
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
    // Error is handled via notification manager and displayed in StartScreen
  }

  fun showConnectProgressDialog() {
    connectDialog = ProgressModal.show(this, resources.getString(R.string.start_starting_vpn))
  }

  private fun openConfigureActivity() =
    startActivity(Intent(this, ConfigureActivity::class.java))

  private fun openStatusActivity(transition: Boolean = true) {
    val intent = Intent(this, StatusActivity::class.java)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      .apply { if (!transition) addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) }

    startActivity(intent)
  }

  private fun openDocumentTree(documentId: String) {
    try {
      BrowseFilesIntents.openDocumentTree(this, documentId)
    } catch (e: ActivityNotFoundException) {
      runOnUiThread {
        showErrorDialog(
          R.string.configure_browse_directories_error_no_file_manager,
          docTopic = "troubleshooting.html#no-file-manager-found",
        )
      }
    }
  }
}
