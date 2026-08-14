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

package org.pacien.tincapp.ui.netlist

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.netlist.configure.*
import org.pacien.tincapp.ui.common.AppStatusHeader
import org.pacien.tincapp.ui.common.AppActions
import org.pacien.tincapp.ui.common.AppActionsMenu
import org.pacien.tincapp.ui.common.SectionTitle
import org.pacien.tincapp.ui.theme.ActivityHorizontalMargin
import org.pacien.tincapp.ui.theme.ActivityVerticalMargin
import org.pacien.tincapp.ui.theme.BackgroundSecondary
import org.pacien.tincapp.ui.theme.TincTheme
import org.pacien.tincapp.repository.VpnState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(
    viewModel: NetworkListViewModel,
    configureViewModel: ConfigureViewModel,
    vpnState: VpnState,
    onNetworkClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    appActions: AppActions
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NetworkListScreenContent(
        vpnState = vpnState,
        networkList = uiState.networkList,
        onNetworkClick = onNetworkClick,
        onConfigDirClick = onConfigDirClick,
        appActions = appActions,
        onGenerateConfigClick = { viewModel.showDialog(ConfigureDialogType.GENERATE) },
        onJoinNetworkClick = { viewModel.showDialog(ConfigureDialogType.JOIN) },
        onLockClick = { viewModel.showDialog(ConfigureDialogType.LOCK, it) },
        onUnlockClick = { viewModel.showDialog(ConfigureDialogType.UNLOCK, it) },
        onDeleteClick = { viewModel.showDialog(ConfigureDialogType.DELETE, it) }
    )

    when (uiState.activeDialog) {
        ConfigureDialogType.GENERATE -> GenerateConfigDialog(
            viewModel = configureViewModel,
            onDismiss = { viewModel.dismissDialog() }
        )
        ConfigureDialogType.JOIN -> JoinNetworkDialog(
            viewModel = configureViewModel,
            onDismiss = { viewModel.dismissDialog() }
        )
        ConfigureDialogType.LOCK -> LockNetworkDialog(
            netName = uiState.selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = { viewModel.dismissDialog() }
        )
        ConfigureDialogType.UNLOCK -> UnlockNetworkDialog(
            netName = uiState.selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = { viewModel.dismissDialog() }
        )
        ConfigureDialogType.DELETE -> DeleteNetworkDialog(
            netName = uiState.selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = { viewModel.dismissDialog() }
        )
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreenContent(
    vpnState: VpnState,
    networkList: List<NetworkInfo>,
    onNetworkClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    appActions: AppActions,
    onGenerateConfigClick: () -> Unit,
    onJoinNetworkClick: () -> Unit,
    onLockClick: (String) -> Unit,
    onUnlockClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var showCreateMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AppStatusHeader(vpnState)
                },
                actions = {
                    IconButton(onClick = { showCreateMenu = !showCreateMenu }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.configure_tools_generate_config_title),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    DropdownMenu(
                        expanded = showCreateMenu,
                        onDismissRequest = { showCreateMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_tools_action_create_node_configuration_and_keys)) },
                            onClick = {
                                showCreateMenu = false
                                onGenerateConfigClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_tools_action_join_network_via_invitation_url)) },
                            onClick = {
                                showCreateMenu = false
                                onJoinNetworkClick()
                            }
                        )
                    }
                    AppActionsMenu(
                        actions = appActions
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding()
                )
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                item {
                    SectionTitle(text = stringResource(R.string.start_network_list_title))
                }

                if (networkList.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.start_network_list_empty_none_found),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = ActivityHorizontalMargin
                            )
                        )
                    }
                } else {
                    items(networkList, key = { it.name }) { network ->
                        NetworkListItem(
                            name = network.name,
                            needsPassphrase = network.needsPassphrase,
                            onClick = { onNetworkClick(network.name) },
                            onConfigDirClick = { onConfigDirClick(network.name) },
                            onLockClick = { onLockClick(network.name) },
                            onUnlockClick = { onUnlockClick(network.name) },
                            onDeleteClick = { onDeleteClick(network.name) }
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(BackgroundSecondary)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(
                        start = ActivityHorizontalMargin,
                        end = ActivityHorizontalMargin,
                        top = ActivityVerticalMargin
                    )
            ) {
                Icon(Icons.Outlined.Info, null)
                Spacer(modifier = Modifier.width(ActivityHorizontalMargin))
                Text(
                    text = stringResource(R.string.start_network_list_warning_text),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun NetworkListItem(
    name: String,
    needsPassphrase: Boolean,
    onClick: () -> Unit,
    onConfigDirClick: () -> Unit,
    onLockClick: () -> Unit,
    onUnlockClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    ListItem(
        leadingContent = { Icon(Icons.Default.Link, null) },
        headlineContent = { Text(name) },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = { showMenu = true }
        ),
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.generic_action_menu)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        text = { Text(stringResource(R.string.start_network_action_connect)) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        text = { Text(stringResource(R.string.start_network_action_edit_config)) },
                        onClick = {
                            showMenu = false
                            onConfigDirClick()
                        }
                    )
                    if (needsPassphrase) {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                            text = { Text(stringResource(R.string.start_network_action_unlock)) },
                            onClick = {
                                showMenu = false
                                onUnlockClick()
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Default.EnhancedEncryption, null) },
                            text = { Text(stringResource(R.string.start_network_action_lock)) },
                            onClick = {
                                showMenu = false
                                onLockClick()
                            }
                        )
                    }
                    DropdownMenuItem(
                        leadingIcon = { Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        ) },
                        text = { Text(
                            stringResource(R.string.start_network_action_delete),
                            color = MaterialTheme.colorScheme.error
                        ) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NetworkListScreenPreview() {
    TincTheme {
        NetworkListScreenContent(
            networkList = listOf(
                NetworkInfo("Aperture Laboratories", false),
                NetworkInfo("Black Mesa", true)
            ),
            onNetworkClick = {},
            onConfigDirClick = {},
            appActions = AppActions({}, {}, {}, {}),
            onGenerateConfigClick = {},
            onJoinNetworkClick = {},
            onLockClick = {},
            onUnlockClick = {},
            onDeleteClick = {},
            vpnState = VpnState.Disconnected,
        )
    }
}
