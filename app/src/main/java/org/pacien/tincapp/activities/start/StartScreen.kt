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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppNotificationManager.ErrorNotification
import org.pacien.tincapp.ui.components.SectionTitle
import org.pacien.tincapp.ui.theme.TincTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    viewModel: StartViewModel,
    onConfigureClick: () -> Unit,
    onAboutClick: () -> Unit,
    onNetworkClick: (String) -> Unit,
    onManualClick: (String) -> Unit,
    onConfigDirClick: (String) -> Unit,
    onLogDirClick: () -> Unit
) {
    val networkList by viewModel.networkList.observeAsState(emptyList())
    val errorNotification by viewModel.errorNotification.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onConfigureClick) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = stringResource(R.string.start_activity_menu_configure),
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onAboutClick) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = stringResource(R.string.about_app_menu_about),
                            tint = Color.White
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
        // Mock ViewModel or use a dummy state
    }
}
