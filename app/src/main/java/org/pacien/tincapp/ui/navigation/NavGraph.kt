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

package org.pacien.tincapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.pacien.tincapp.activities.configure.ConfigureViewModel
import org.pacien.tincapp.activities.start.StartScreen
import org.pacien.tincapp.activities.start.StartViewModel
import org.pacien.tincapp.activities.status.nodes.NodeListViewModel
import org.pacien.tincapp.activities.status.subnets.SubnetListViewModel
import org.pacien.tincapp.activities.status.ui.NodeInfoDialog
import org.pacien.tincapp.activities.status.ui.StatusScreen
import org.pacien.tincapp.activities.status.viewlog.LogViewModel
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.context.App
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.storageprovider.FilesDocumentsProvider

@Composable
fun NavGraph(
    navController: NavHostController,
    onAboutClick: () -> Unit,
    onShowError: (String, String?) -> Unit,
    onOpenDocumentTree: (String) -> Unit,
    onNetworkClick: (String) -> Unit,
    onDisconnectClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (TincVpnService.isConnected()) "status" else "start"
    ) {
        composable("start") {
            val viewModel: StartViewModel = viewModel()
            val configureViewModel: ConfigureViewModel = viewModel()
            StartScreen(
                viewModel = viewModel,
                configureViewModel = configureViewModel,
                onAboutClick = onAboutClick,
                onNetworkClick = onNetworkClick,
                onManualClick = { App.openURL(it) },
                onConfigDirClick = { onOpenDocumentTree(BrowseFilesIntents.networkConfigDirDocId(it)) },
                onLogDirClick = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) },
                onOpenNetworksDirClick = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS) }
            )
        }

        composable("status") {
            val nodeListViewModel: NodeListViewModel = viewModel()
            val subnetListViewModel: SubnetListViewModel = viewModel()
            val logViewModel: LogViewModel = viewModel()
            val vpnService = TincVpnService
            val netName = vpnService.getCurrentNetName()
            val vpnCfg = vpnService.getCurrentInterfaceCfg() ?: org.pacien.tincapp.data.VpnInterfaceConfiguration()

            var selectedNodeInfo by remember { mutableStateOf<String?>(null) }

            StatusScreen(
                vpnInterfaceConfiguration = vpnCfg,
                nodeListLiveData = nodeListViewModel.nodeList,
                subnetListLiveData = subnetListViewModel.nodeList,
                logLiveData = logViewModel.log,
                onNodeClick = { node ->
                    Tinc.info(netName!!, node.name).thenAccept { info ->
                        selectedNodeInfo = info
                    }
                },
                netName = netName,
                onDisconnectClick = onDisconnectClick,
                onAboutClick = onAboutClick,
                onOpenNetworksDirClick = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS) },
                onLogDirClick = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) }
            )

            selectedNodeInfo?.let { info ->
                NodeInfoDialog(
                    nodeInfoText = info,
                    onDismiss = { selectedNodeInfo = null }
                )
            }
        }
    }
}
