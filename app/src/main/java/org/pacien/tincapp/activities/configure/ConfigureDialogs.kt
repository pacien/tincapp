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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.zxing.integration.android.IntentIntegrator
import org.pacien.tincapp.R

@Composable
fun GenerateConfigDialog(
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var netName by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }

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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.configure_tools_generate_config_optional_passphrase_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.generateConf(netName, nodeName, passphrase) },
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
    var passphrase by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val intentResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
            if (intentResult != null && intentResult.contents != null) {
                invitationUrl = intentResult.contents.trim()
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
                        context.findActivity()?.let { activity ->
                            val intent = IntentIntegrator(activity)
                                .setOrientationLocked(false)
                                .setPrompt(scanPrompt)
                                .createScanIntent()
                            scanLauncher.launch(intent)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = stringResource(R.string.configure_tools_join_network_action_scan_qr_code_action),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.configure_tools_generate_config_optional_passphrase_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.joinNetwork(netName, invitationUrl, passphrase) },
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
fun EncryptDecryptKeysDialog(
    viewModel: ConfigureViewModel,
    onDismiss: () -> Unit
) {
    var netName by remember { mutableStateOf("") }
    var currentPassphrase by remember { mutableStateOf("") }
    var newPassphrase by remember { mutableStateOf("") }

    ConfigureToolsState(viewModel, onSuccess = onDismiss)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.configure_tools_private_keys_encryption_title)) },
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
                    value = currentPassphrase,
                    onValueChange = { currentPassphrase = it },
                    label = { Text(stringResource(R.string.configure_tools_private_key_encryption_current_passphrase)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassphrase,
                    onValueChange = { newPassphrase = it },
                    label = { Text(stringResource(R.string.configure_tools_private_key_encryption_new_passphrase)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { viewModel.encryptDecryptPrivateKeys(netName, currentPassphrase, newPassphrase) },
                enabled = netName.isNotBlank() && (currentPassphrase.isNotEmpty() || newPassphrase.isNotEmpty())
            ) {
                Text(stringResource(R.string.configure_tools_private_keys_encryption_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_action_close))
            }
        }
    )
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
