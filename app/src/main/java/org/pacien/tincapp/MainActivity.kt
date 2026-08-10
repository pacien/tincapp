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

package org.pacien.tincapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.pacien.tincapp.activities.common.RecentCrashHandler
import org.pacien.tincapp.activities.start.ConnectionStarter
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppInfo
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.intent.BroadcastMapper
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.ui.components.DecryptKeysDialog
import org.pacien.tincapp.ui.components.ProgressDialog
import org.pacien.tincapp.ui.navigation.NavGraph
import org.pacien.tincapp.ui.theme.TincTheme

class MainActivity : ComponentActivity() {

    private lateinit var navController: NavHostController

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            connectionStarter.tryStart()
        } else {
            App.alert(R.string.notification_error_title_unable_to_start_tinc, getString(R.string.notification_error_message_could_not_bind_iface))
        }
    }

    private val connectionStarter by lazy { ConnectionStarter(this) }

    private val broadcastMapper = BroadcastMapper(mapOf(
        Actions.EVENT_CONNECTED to ::onVpnStart,
        Actions.EVENT_ABORTED to ::onVpnStartError,
        Actions.EVENT_DISCONNECTED to ::onVpnShutdown
    ))

    private var activeDialog by mutableStateOf<ActiveDialog?>(null)

    sealed class ActiveDialog {
        data class Progress(val text: String) : ActiveDialog()
        object DecryptKeys : ActiveDialog()
        object About : ActiveDialog()
        data class Error(val msg: String, val docTopic: String? = null) : ActiveDialog()
        data class Crash(val message: String, val onSendReport: () -> Unit) : ActiveDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TincTheme {
                navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    onAboutClick = { showAboutDialog() },
                    onShowError = { msg, topic -> showErrorDialog(msg, topic) },
                    onOpenDocumentTree = { openDocumentTree(it) },
                    onNetworkClick = { connectionStarter.tryStart(it, displayStatus = true) },
                    onDisconnectClick = { stopVpn() }
                )

                when (val dialog = activeDialog) {
                    is ActiveDialog.Progress -> ProgressDialog(dialog.text)
                    is ActiveDialog.DecryptKeys -> DecryptKeysDialog(
                        onDismiss = { activeDialog = null },
                        onConfirm = {
                            activeDialog = null
                            connectionStarter.tryStart(passphrase = it)
                        }
                    )
                    is ActiveDialog.About -> org.pacien.tincapp.ui.components.AboutDialog(
                        onDismiss = { activeDialog = null },
                        onOpenWebsite = { App.openURL(getString(R.string.about_app_website_url)) }
                    )
                    is ActiveDialog.Error -> org.pacien.tincapp.ui.components.ErrorDialog(
                        message = dialog.msg,
                        docTopic = dialog.docTopic,
                        onDismiss = { activeDialog = null },
                        onOpenManual = { App.openURL(getString(R.string.app_doc_url_format, it)) }
                    )
                    is ActiveDialog.Crash -> org.pacien.tincapp.ui.components.CrashDialog(
                        message = dialog.message,
                        onDismiss = { activeDialog = null },
                        onSendReport = {
                            activeDialog = null
                            dialog.onSendReport()
                        }
                    )
                    null -> {}
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Actions.ACTION_CONNECT -> {
                if (intent.data?.schemeSpecificPart != null) {
                    connectionStarter.tryStart(intent.data!!.schemeSpecificPart, intent.data!!.fragment, false)
                }
            }
            Actions.ACTION_DISCONNECT -> {
                stopVpn()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        broadcastMapper.register(lifecycleScope)
        
        RecentCrashHandler(this).handleRecentCrash()?.let {
            activeDialog = ActiveDialog.Crash(it.message, it.onSendReport)
        }

        if (::navController.isInitialized && TincVpnService.isConnected() && navController.currentDestination?.route == "start") {
            navController.navigate("status") {
                popUpTo("start") { inclusive = true }
            }
        }
    }

    override fun onPause() {
        broadcastMapper.unregister()
        super.onPause()
    }

    private fun onVpnStart() {
        activeDialog = null
        if (::navController.isInitialized) {
            navController.navigate("status") {
                popUpTo("start") { inclusive = true }
            }
        }
    }

    private fun onVpnStartError() {
        activeDialog = null
    }

    private fun onVpnShutdown() {
        activeDialog = null
        if (::navController.isInitialized) {
            navController.navigate("start") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun showConnectProgressDialog() {
        activeDialog = ActiveDialog.Progress(getString(R.string.start_starting_vpn))
    }

    fun showDecryptKeysDialog() {
        activeDialog = ActiveDialog.DecryptKeys
    }

    private fun stopVpn() {
        activeDialog = ActiveDialog.Progress(getString(R.string.status_activity_disconnecting_vpn))
        TincVpnService.disconnect()
    }

    fun showAboutDialog() {
        activeDialog = ActiveDialog.About
    }

    fun showErrorDialog(msg: String, docTopic: String? = null) {
        activeDialog = ActiveDialog.Error(msg, docTopic)
    }

    private fun openDocumentTree(documentId: String) {
        try {
            BrowseFilesIntents.openDocumentTree(this, documentId)
        } catch (e: ActivityNotFoundException) {
            showErrorDialog(
                getString(R.string.configure_browse_directories_error_no_file_manager),
                docTopic = "troubleshooting.html#no-file-manager-found",
            )
        }
    }

    // Helper for ConnectionStarter to request VPN permission
    fun requestVpnPermission(intent: Intent) {
        vpnPermissionLauncher.launch(intent)
    }
}
