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

package org.pacien.tincapp.activities.configure

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.pacien.tincapp.R

@Composable
fun GenerateConfigDialog(
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var netName by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("") }

    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_generate_config_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = netName,
                    onValueChange = { netName = it },
                    label = { Text(stringResource(R.string.configure_tools_generate_config_net_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nodeName,
                    onValueChange = { nodeName = it },
                    label = { Text(stringResource(R.string.configure_tools_generate_config_node_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.generateConf(netName, nodeName) },
                enabled = netName.isNotBlank() && nodeName.isNotBlank()
            ) {
                Text(stringResource(R.string.configure_tools_generate_config_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_close))
            }
        }
    )
}

@Composable
fun JoinNetworkDialog(
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var netName by remember { mutableStateOf("") }
    var invitationUrl by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                invitationUrl = result.contents.trim()
            }
        }
    )

    val scanPrompt = stringResource(R.string.configure_tools_join_network_action_scan_qr_code_action)

    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_join_network_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = netName,
                    onValueChange = { netName = it },
                    label = { Text(stringResource(R.string.configure_tools_generate_config_net_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = invitationUrl,
                        onValueChange = { invitationUrl = it },
                        label = { Text(stringResource(R.string.configure_tools_join_network_field_invitation_url_hint)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(onClick = {
                        val options = ScanOptions()
                            .setOrientationLocked(false)
                            .setPrompt(scanPrompt)
                        scanLauncher.launch(options)
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.configure_tools_join_network_action_scan_qr_code_action),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.joinNetwork(netName, invitationUrl) },
                enabled = netName.isNotBlank() && invitationUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.configure_tools_join_network_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_close))
            }
        }
    )
}

@Composable
fun LockNetworkDialog(
    netName: String,
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var passphrase by remember { mutableStateOf("") }

    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_lock_network_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = netName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.decrypt_key_modal_field_passphrase)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.lockNetwork(netName, passphrase) },
                enabled = passphrase.isNotEmpty()
            ) {
                Text(stringResource(R.string.configure_tools_lock_network_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_cancel))
            }
        }
    )
}

@Composable
fun UnlockNetworkDialog(
    netName: String,
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var passphrase by remember { mutableStateOf("") }

    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_unlock_network_title)) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = netName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.decrypt_key_modal_field_passphrase)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.unlockNetwork(netName, passphrase) },
                enabled = passphrase.isNotEmpty()
            ) {
                Text(stringResource(R.string.configure_tools_unlock_network_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_cancel))
            }
        }
    )
}

@Composable
fun DeleteNetworkDialog(
    netName: String,
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_delete_network_title)) },
        text = {
            Text(stringResource(R.string.configure_tools_delete_network_message_format, netName))
        },
        confirmButton = {
            TextButton(onClick = { viewModel.deleteNetwork(netName) }) {
                Text(
                    text = stringResource(R.string.configure_tools_delete_network_action),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_cancel))
            }
        }
    )
}
