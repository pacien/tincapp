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

package org.pacien.tincapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import org.pacien.tincapp.ui.netlist.configure.ConfigureViewModel
import org.pacien.tincapp.ui.netlist.NetworkListScreen
import org.pacien.tincapp.ui.netlist.StartViewModel
import org.pacien.tincapp.ui.status.StatusViewModel
import org.pacien.tincapp.ui.status.NodeInfoDialog
import org.pacien.tincapp.ui.status.StatusScreen
import org.pacien.tincapp.R
import org.pacien.tincapp.config.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.common.AppActions
import org.pacien.tincapp.intent.openURL
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.storageprovider.FilesDocumentsProvider

@Serializable object NetworkList
@Serializable object Status

@Composable
fun NavGraph(
    navController: NavHostController,
    vpnState: VpnState,
    onAboutClick: () -> Unit,
    onShowError: (String, String?) -> Unit,
    onOpenDocumentTree: (String) -> Unit,
    onNetworkClick: (String) -> Unit,
    onDisconnectClick: () -> Unit
) {
    val context = LocalContext.current

    val appActions = remember(onAboutClick, onShowError, onOpenDocumentTree) {
        AppActions(
            onOpenNetworksDir = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS) },
            onLogDir = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) },
            onManual = { topic ->
                val url = context.getString(R.string.app_doc_url_format, topic ?: "doc.html")
                context.openURL(url, onError = { msg -> onShowError(msg, null) })
            },
            onAbout = onAboutClick
        )
    }

    LaunchedEffect(vpnState) {
        when (vpnState) {
            is VpnState.Connected -> {
                if (navController.currentDestination?.hasRoute<NetworkList>() == true) {
                    navController.navigate(Status) {
                        popUpTo<NetworkList> { inclusive = true }
                    }
                }
            }
            is VpnState.Disconnected -> {
                if (navController.currentDestination?.hasRoute<Status>() == true) {
                    navController.navigate(NetworkList) {
                        popUpTo<Status> { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (VpnRepository.isConnected) Status else NetworkList
    ) {
        composable<NetworkList> {
            val viewModel: StartViewModel = viewModel()
            val configureViewModel: ConfigureViewModel = viewModel()
            NetworkListScreen(
                viewModel = viewModel,
                configureViewModel = configureViewModel,
                onNetworkClick = onNetworkClick,
                onConfigDirClick = { onOpenDocumentTree(BrowseFilesIntents.networkConfigDirDocId(it)) },
                appActions = appActions,
                vpnState = vpnState
            )
        }

        composable<Status> {
            val statusViewModel: StatusViewModel = viewModel()
            val uiState by statusViewModel.uiState.collectAsStateWithLifecycle()
            val vpnCfg = VpnRepository.interfaceConfig ?: VpnInterfaceConfiguration()

            StatusScreen(
                vpnInterfaceConfiguration = vpnCfg,
                uiState = uiState,
                onNodeClick = { statusViewModel.onNodeClick(it) },
                onDisconnectClick = onDisconnectClick,
                appActions = appActions
            )

            uiState.selectedNodeInfo?.let { info ->
                NodeInfoDialog(
                    nodeInfoText = info,
                    onDismiss = { statusViewModel.dismissNodeInfo() }
                )
            }
        }
    }
}
