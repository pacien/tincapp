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
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.pacien.tincapp.intent.VpnState
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hasRoute
import org.pacien.tincapp.activities.common.RecentCrashHandler
import org.pacien.tincapp.activities.start.ConnectionStarter
import org.pacien.tincapp.context.App
import org.pacien.tincapp.intent.Actions
import org.pacien.tincapp.intent.openURL
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.storageprovider.FilesDocumentsProvider
import org.pacien.tincapp.ui.components.DecryptKeysDialog
import org.pacien.tincapp.ui.components.ProgressDialog
import org.pacien.tincapp.ui.navigation.NavGraph
import org.pacien.tincapp.ui.navigation.Start
import org.pacien.tincapp.ui.navigation.Status
import org.pacien.tincapp.ui.theme.TincTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TincTheme {
                navController = rememberNavController()
                val vpnState by viewModel.vpnState.collectAsStateWithLifecycle()

                NavGraph(
                    navController = navController,
                    vpnState = vpnState,
                    onAboutClick = { viewModel.showAbout() },
                    onShowError = { msg, topic -> viewModel.showError(msg, topic) },
                    onOpenDocumentTree = { openDocumentTree(it) },
                    onNetworkClick = { connectionStarter.tryStart(it, displayStatus = true) },
                    onDisconnectClick = { stopVpn() }
                )

                when (val dialog = viewModel.activeDialog) {
                    is MainViewModel.ActiveDialog.Progress -> ProgressDialog(dialog.text)
                    is MainViewModel.ActiveDialog.DecryptKeys -> DecryptKeysDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onConfirm = {
                            viewModel.dismissDialog()
                            connectionStarter.tryStart(passphrase = it)
                        }
                    )
                    is MainViewModel.ActiveDialog.About -> org.pacien.tincapp.ui.components.AboutDialog(
                        onDismiss = { viewModel.dismissDialog() },
                        onOpenWebsite = { openURL(getString(R.string.about_app_website_url), onError = { showErrorDialog(it) }) }
                    )
                    is MainViewModel.ActiveDialog.Error -> org.pacien.tincapp.ui.components.ErrorDialog(
                        message = dialog.msg,
                        docTopic = dialog.docTopic,
                        onDismiss = { viewModel.dismissDialog() },
                        onOpenManual = {
                            val url = getString(R.string.app_doc_url_format, it)
                            openURL(url, onError = { showErrorDialog(it) })
                        }
                    )
                    is MainViewModel.ActiveDialog.Warning -> org.pacien.tincapp.ui.components.WarningDialog(
                        message = dialog.msg,
                        docTopic = dialog.docTopic,
                        onDismiss = { viewModel.dismissDialog() },
                        onContinue = {
                            viewModel.dismissDialog()
                            dialog.onContinue()
                        },
                        onOpenManual = {
                            val url = getString(R.string.app_doc_url_format, it)
                            openURL(url, onError = { showErrorDialog(it) })
                        }
                    )
                    is MainViewModel.ActiveDialog.Crash -> org.pacien.tincapp.ui.components.CrashDialog(
                        message = dialog.message,
                        onDismiss = { viewModel.dismissDialog() },
                        onSendReport = {
                            viewModel.dismissDialog()
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
        
        RecentCrashHandler(this).handleRecentCrash()?.let {
            viewModel.showCrash(it.message, it.onSendReport)
        }
    }

    override fun onPause() {
        super.onPause()
    }

    fun showConnectProgressDialog() {
        viewModel.showProgress(getString(R.string.start_starting_vpn))
    }

    fun showDecryptKeysDialog() {
        viewModel.showDecryptKeys()
    }

    private fun stopVpn() {
        viewModel.showProgress(getString(R.string.status_activity_disconnecting_vpn))
        TincVpnService.disconnect()
    }

    fun showErrorDialog(msg: String, docTopic: String? = null) {
        viewModel.showError(msg, docTopic)
    }

    private fun openDocumentTree(documentId: String) {
        if (documentId.startsWith(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS) && !BrowseFilesIntents.hasTextEditor(this)) {
            viewModel.showWarning(
                getString(R.string.configure_browse_directories_warning_no_text_editor),
                docTopic = "troubleshooting.html#no-text-editor-found",
                onContinue = { doOpenDocumentTree(documentId) }
            )
        } else {
            doOpenDocumentTree(documentId)
        }
    }

    private fun doOpenDocumentTree(documentId: String) {
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
