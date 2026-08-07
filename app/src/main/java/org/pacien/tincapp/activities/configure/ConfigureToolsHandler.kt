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

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.components.ProgressDialog

@Composable
fun ConfigureToolsState(
    viewModel: ConfigureViewModel,
    onSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when (val state = uiState) {
        is ConfigureUiState.Loading -> {
            ProgressDialog(text = stringResource(state.messageResId))
        }
        is ConfigureUiState.Error -> {
            val message = state.message ?: state.messageResId?.let { stringResource(it) } ?: "Unknown error"
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                title = { Text(stringResource(R.string.generic_title_error)) },
                text = { Text(message) },
                confirmButton = {
                    TextButton(onClick = { viewModel.resetState() }) {
                        Text(stringResource(R.string.generic_action_close))
                    }
                }
            )
        }
        is ConfigureUiState.Success -> {
            LaunchedEffect(state) {
                Toast.makeText(context, state.messageResId, Toast.LENGTH_LONG).show()
                viewModel.resetState()
                onSuccess()
            }
        }
        else -> {}
    }
}
