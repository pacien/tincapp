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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TextTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateConfigScreen(
    viewModel: ConfigureViewModel,
    onNavigateBack: () -> Unit
) {
    var netName by remember { mutableStateOf("") }
    var nodeName by remember { mutableStateOf("") }
    var passphrase by remember { mutableStateOf("") }

    ConfigureToolsState(viewModel, onSuccess = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.configure_tools_generate_config_title)) },
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.generateConf(netName, nodeName, passphrase) },
                modifier = Modifier.fillMaxWidth(),
                enabled = netName.isNotBlank() && nodeName.isNotBlank()
            ) {
                Text(stringResource(R.string.configure_tools_generate_config_action))
            }
        }
    }
}
