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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.pacien.tincapp.activities.configure.ConfigureScreen
import org.pacien.tincapp.activities.configure.ConfigureViewModel
import org.pacien.tincapp.activities.configure.EncryptDecryptKeysScreen
import org.pacien.tincapp.activities.configure.GenerateConfigScreen
import org.pacien.tincapp.activities.configure.JoinNetworkScreen
import org.pacien.tincapp.activities.start.StartScreen
import org.pacien.tincapp.activities.start.StartViewModel
import org.pacien.tincapp.activities.status.nodes.NodeListViewModel
import org.pacien.tincapp.activities.status.subnets.SubnetListViewModel
import org.pacien.tincapp.activities.status.ui.NodeInfoDialog
import org.pacien.tincapp.activities.status.ui.StatusScreen
import org.pacien.tincapp.activities.viewlog.LogViewModel
import org.pacien.tincapp.activities.viewlog.ViewLogScreen
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
    onDisconnectClick: () -> Unit,
    onShareLogClick: (String?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (TincVpnService.isConnected()) "status" else "start"
    ) {
        composable("start") {
            val viewModel: StartViewModel = viewModel()
            StartScreen(
                viewModel = viewModel,
                onConfigureClick = { navController.navigate("configure") },
                onAboutClick = onAboutClick,
                onNetworkClick = onNetworkClick,
                onManualClick = { App.openURL(it) },
                onConfigDirClick = { onOpenDocumentTree(BrowseFilesIntents.networkConfigDirDocId(it)) },
                onLogDirClick = { onOpenDocumentTree(FilesDocumentsProvider.VIRTUAL_ROOT_LOGS) }
            )
        }

        composable("configure") {
            ConfigureScreen(
                onOpenDirectory = onOpenDocumentTree,
                onGenerateConfig = { navController.navigate("configure_generate") },
                onJoinNetwork = { navController.navigate("configure_join") },
                onEncryptDecryptKeys = { navController.navigate("configure_keys") },
                onNavigateUp = { navController.popBackStack() }
            )
        }

        composable("configure_generate") {
            val viewModel: ConfigureViewModel = viewModel()
            GenerateConfigScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("configure_join") {
            val viewModel: ConfigureViewModel = viewModel()
            JoinNetworkScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("configure_keys") {
            val viewModel: ConfigureViewModel = viewModel()
            EncryptDecryptKeysScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("status") {
            val nodeListViewModel: NodeListViewModel = viewModel()
            val subnetListViewModel: SubnetListViewModel = viewModel()
            val vpnService = TincVpnService
            val netName = vpnService.getCurrentNetName()
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
                        selectedNodeInfo = info
                    }
                },
                netName = netName,
                onDisconnectClick = onDisconnectClick,
                onShowLogClick = { navController.navigate("viewlog") },
                onAboutClick = onAboutClick
            )

            selectedNodeInfo?.let { info ->
                NodeInfoDialog(
                    nodeInfoText = info,
                    onDismiss = { selectedNodeInfo = null }
                )
            }
        }

        composable("viewlog") {
            val viewModel: LogViewModel = viewModel()
            val logLines by viewModel.log.observeAsState(emptyList())
            val logText = logLines.joinToString("\n\n")
            var isLoggingEnabled by remember { mutableStateOf(viewModel.logging) }

            ViewLogScreen(
                logText = logText,
                logLevelText = viewModel.logLevelText,
                isLoggingEnabled = isLoggingEnabled,
                onBackClick = { navController.popBackStack() },
                onToggleLogging = {
                    isLoggingEnabled = !isLoggingEnabled
                    viewModel.logging = isLoggingEnabled
                },
                onShareClick = { onShareLogClick(viewModel.log.value?.joinToString("\n")) }
            )
        }
    }
}
