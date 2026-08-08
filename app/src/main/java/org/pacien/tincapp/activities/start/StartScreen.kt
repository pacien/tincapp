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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
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
import org.pacien.tincapp.activities.configure.ConfigureViewModel
import org.pacien.tincapp.activities.configure.EncryptDecryptKeysDialog
import org.pacien.tincapp.activities.configure.GenerateConfigDialog
import org.pacien.tincapp.activities.configure.JoinNetworkDialog
import org.pacien.tincapp.context.AppNotificationManager.ErrorNotification
import org.pacien.tincapp.ui.components.SectionTitle
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
        onEncryptDecryptKeysClick = { activeDialog = ConfigureDialogType.ENCRYPT_DECRYPT }
    )

    when (activeDialog) {
        ConfigureDialogType.GENERATE -> GenerateConfigDialog(
            viewModel = configureViewModel,
            onDismiss = { activeDialog = null }
        )
        ConfigureDialogType.JOIN -> JoinNetworkDialog(
            viewModel = configureViewModel,
            onDismiss = { activeDialog = null }
        )
        ConfigureDialogType.ENCRYPT_DECRYPT -> EncryptDecryptKeysDialog(
            viewModel = configureViewModel,
            onDismiss = { activeDialog = null }
        )
        null -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreenContent(
    networkList: List<String>,
    errorNotification: ErrorNotification?,
    onAboutClick: () -> Unit,
    onNetworkClick: (String) -> Unit,
    onManualClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    onLogDirClick: () -> Unit,
    onOpenNetworksDirClick: () -> Unit,
    onGenerateConfigClick: () -> Unit,
    onJoinNetworkClick: () -> Unit,
    onEncryptDecryptKeysClick: () -> Unit
) {
    var showCreateMenu by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
                            text = { Text(stringResource(R.string.configure_tools_action_generate_node_configuration_and_keys)) },
                            onClick = {
                                showCreateMenu = false
                                onGenerateConfigClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_tools_action_join_network_via_invitation_url_qr_code)) },
                            onClick = {
                                showCreateMenu = false
                                onJoinNetworkClick()
                            }
                        )
                    }
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.generic_action_menu),
                            tint = Color.White
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_browse_directories_action_open_networks_config)) },
                            onClick = {
                                showMenu = false
                                onOpenNetworksDirClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_browse_directories_action_open_logs)) },
                            onClick = {
                                showMenu = false
                                onLogDirClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.configure_tools_action_encrypt_decrypt_private_keys)) },
                            onClick = {
                                showMenu = false
                                onEncryptDecryptKeysClick()
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.about_app_menu_about)) },
                            onClick = {
                                showMenu = false
                                onAboutClick()
                            }
                        )
                    }
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
                items(networkList) { network ->
                    NetworkListItem(
                        name = network,
                        onClick = { onNetworkClick(network) }
                    )
                }
            }
        }
    }
}


enum class ConfigureDialogType {
    GENERATE, JOIN, ENCRYPT_DECRYPT
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
fun NetworkListItem(name: String, onClick: () -> Unit) {
    Text(
        text = name,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                vertical = 16.dp
            )
    )
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    TincTheme {
        StartScreenContent(
            networkList = listOf("Aperture Laboratories", "Black Mesa"),
            errorNotification = null,
            onAboutClick = {},
            onNetworkClick = {},
            onManualClick = {},
            onConfigDirClick = {},
            onLogDirClick = {},
            onOpenNetworksDirClick = {},
            onGenerateConfigClick = {},
            onJoinNetworkClick = {},
            onEncryptDecryptKeysClick = {}
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
            onEncryptDecryptKeysClick = {}
        )
    }
}
