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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinNetworkScreen(
    viewModel: ConfigureViewModel,
    onNavigateBack: () -> Unit
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

    ConfigureToolsState(viewModel, onSuccess = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configure_tools_join_network_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.generic_action_close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HeaderPrimary,
                    titleContentColor = TextTitle,
                    navigationIconContentColor = TextTitle
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.joinNetwork(netName, invitationUrl, passphrase) },
                modifier = Modifier.fillMaxWidth(),
                enabled = netName.isNotBlank() && invitationUrl.isNotBlank()
            ) {
                Text(stringResource(R.string.configure_tools_join_network_action))
            }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
