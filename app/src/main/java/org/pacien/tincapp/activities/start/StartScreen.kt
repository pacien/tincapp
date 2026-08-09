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

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.configure.*
import org.pacien.tincapp.context.AppNotificationManager.ErrorNotification
import org.pacien.tincapp.ui.components.*
import org.pacien.tincapp.ui.theme.TincTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    viewModel: StartViewModel,
    configureViewModel: ConfigureViewModel,
    onAboutClick: () -> Unit,
    onNetworkClick: (String) -> Unit,
    onManualClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    onLogDirClick: () -> Unit,
    onOpenNetworksDirClick: () -> Unit
) {
    val networkList by viewModel.networkList.observeAsState(emptyList())
    val errorNotification by viewModel.errorNotification.observeAsState()

    var activeDialog by remember { mutableStateOf<ConfigureDialogType?>(null) }
    var selectedNetwork by remember { mutableStateOf("") }

    StartScreenContent(
        networkList = networkList,
        errorNotification = errorNotification,
        onAboutClick = onAboutClick,
        onNetworkClick = onNetworkClick,
        onManualClick = onManualClick,
        onConfigDirClick = onConfigDirClick,
        onLogDirClick = onLogDirClick,
        onOpenNetworksDirClick = onOpenNetworksDirClick,
        onGenerateConfigClick = { activeDialog = ConfigureDialogType.GENERATE },
        onJoinNetworkClick = { activeDialog = ConfigureDialogType.JOIN },
        onLockClick = {
            selectedNetwork = it
            activeDialog = ConfigureDialogType.LOCK
        },
        onUnlockClick = {
            selectedNetwork = it
            activeDialog = ConfigureDialogType.UNLOCK
        },
        onDeleteClick = {
            selectedNetwork = it
            activeDialog = ConfigureDialogType.DELETE
        }
    )

    when (activeDialog) {
        ConfigureDialogType.GENERATE -> GenerateConfigDialog(
            viewModel = configureViewModel,
            onDismiss = {
                activeDialog = null
                viewModel.refresh()
            }
        )
        ConfigureDialogType.JOIN -> JoinNetworkDialog(
            viewModel = configureViewModel,
            onDismiss = {
                activeDialog = null
                viewModel.refresh()
            }
        )
        ConfigureDialogType.LOCK -> LockNetworkDialog(
            netName = selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = {
                activeDialog = null
                viewModel.refresh()
            }
        )
        ConfigureDialogType.UNLOCK -> UnlockNetworkDialog(
            netName = selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = {
                activeDialog = null
                viewModel.refresh()
            }
        )
        ConfigureDialogType.DELETE -> DeleteNetworkDialog(
            netName = selectedNetwork,
            viewModel = configureViewModel,
            onDismiss = {
                activeDialog = null
                viewModel.refresh()
            }
        )
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreenContent(
    networkList: List<NetworkInfo>,
    errorNotification: ErrorNotification?,
    onAboutClick: () -> Unit,
    onNetworkClick: (String) -> Unit,
    onManualClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    onLogDirClick: () -> Unit,
    onOpenNetworksDirClick: () -> Unit,
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
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showCreateMenu = !showCreateMenu }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.configure_tools_generate_config_title),
                            tint = Color.White
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
                    GenericActionsMenu(
                        onOpenNetworksDirClick = onOpenNetworksDirClick,
                        onLogDirClick = onLogDirClick,
                        onAboutClick = onAboutClick
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                Text(
                    text = stringResource(R.string.start_network_list_warning_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(
                        horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                        vertical = dimensionResource(R.dimen.activity_vertical_margin)
                    )
                )
            }

            errorNotification?.let { error ->
                item {
                    ErrorNotificationBlock(
                        error = error,
                        onManualClick = { error.manualLink?.let { onManualClick(it) } },
                        onConfigDirClick = { error.configDir?.let { onConfigDirClick(it) } },
                        onLogDirClick = onLogDirClick
                    )
                }
            }

            item {
                SectionTitle(text = stringResource(R.string.start_network_list_title))
            }

            if (networkList.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.start_network_list_empty_none_found),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(dimensionResource(R.dimen.activity_horizontal_margin))
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
    }
}


enum class ConfigureDialogType {
    GENERATE, JOIN, LOCK, UNLOCK, DELETE
}

@Composable
fun ErrorNotificationBlock(
    error: ErrorNotification,
    onManualClick: () -> Unit,
    onConfigDirClick: () -> Unit,
    onLogDirClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = error.title,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.activity_horizontal_margin))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(
                    horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                    vertical = 8.dp
                )
            )

            error.manualLink?.let {
                ClickableBlock(
                    text = stringResource(R.string.notification_error_action_open_manual),
                    onClick = onManualClick
                )
            }

            error.configDir?.let {
                ClickableBlock(
                    text = stringResource(R.string.notification_error_action_open_config_dir),
                    onClick = onConfigDirClick
                )
            }

            if (error.proposeLogs) {
                ClickableBlock(
                    text = stringResource(R.string.notification_error_action_open_logs_dir),
                    onClick = onLogDirClick
                )
            }
        }
    }
}

@Composable
fun ClickableBlock(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                vertical = 12.dp
            )
    )
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
fun StartScreenPreview() {
    TincTheme {
        StartScreenContent(
            networkList = listOf(
                NetworkInfo("Aperture Laboratories", false),
                NetworkInfo("Black Mesa", true)
            ),
            errorNotification = null,
            onAboutClick = {},
            onNetworkClick = {},
            onManualClick = {},
            onConfigDirClick = {},
            onLogDirClick = {},
            onOpenNetworksDirClick = {},
            onGenerateConfigClick = {},
            onJoinNetworkClick = {},
            onLockClick = {},
            onUnlockClick = {},
            onDeleteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenErrorPreview() {
    TincTheme {
        StartScreenContent(
            networkList = emptyList(),
            errorNotification = ErrorNotification(
                title = "Configuration Error",
                message = "The configuration file 'tinc.conf' is missing. Please check the configuration directory.",
                manualLink = "",
                configDir = "",
                proposeLogs = true
            ),
            onAboutClick = {},
            onNetworkClick = {},
            onManualClick = {},
            onConfigDirClick = {},
            onLogDirClick = {},
            onOpenNetworksDirClick = {},
            onGenerateConfigClick = {},
            onJoinNetworkClick = {},
            onLockClick = {},
            onUnlockClick = {},
            onDeleteClick = {}
        )
    }
}
