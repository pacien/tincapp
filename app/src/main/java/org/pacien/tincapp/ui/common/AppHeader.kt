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

package org.pacien.tincapp.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppInfo
import org.pacien.tincapp.repository.VpnState

@Composable
fun AppStatusHeader(
  vpnState: VpnState
) {
  Column {
    Text(
      text = stringResource(R.string.app_name),
      style = MaterialTheme.typography.titleLarge
    )
    Text(
      text = when (vpnState) {
        is VpnState.Connected -> stringResource(
          R.string.status_activity_state_connected_to_format,
          vpnState.netName
        )

        else -> stringResource(R.string.status_activity_state_disconnected)
      },
      style = MaterialTheme.typography.bodyMedium
    )
  }
}

data class AppActions(
  val onOpenNetworksDir: () -> Unit,
  val onLogDir: () -> Unit,
  val onManual: (String?) -> Unit,
  val onAbout: () -> Unit
)

@Composable
fun AppActionsMenu(
  actions: AppActions,
  tint: Color = Color.White
) {
  var showMenu by remember { mutableStateOf(false) }

  Box {
    IconButton(onClick = { showMenu = !showMenu }) {
      Icon(
        imageVector = Icons.Default.MoreVert,
        contentDescription = stringResource(R.string.generic_action_menu),
        tint = tint
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
          actions.onOpenNetworksDir()
        }
      )
      DropdownMenuItem(
        text = { Text(stringResource(R.string.configure_browse_directories_action_open_logs)) },
        onClick = {
          showMenu = false
          actions.onLogDir()
        }
      )
      MenuDivider()
      DropdownMenuItem(
        text = { Text(stringResource(R.string.about_app_menu_manual)) },
        onClick = {
          showMenu = false
          actions.onManual(null)
        }
      )
      DropdownMenuItem(
        text = { Text(stringResource(R.string.about_app_menu_about)) },
        onClick = {
          showMenu = false
          actions.onAbout()
        }
      )
    }
  }
}

@Composable
fun AboutDialog(
  onDismiss: () -> Unit,
  onOpenWebsite: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.app_name)) },
    text = {
      Text(
        text = stringResource(R.string.about_app_short_desc) + "\n\n" +
          stringResource(R.string.about_app_copyright) + " " +
          stringResource(R.string.about_app_license) + "\n\n" +
          AppInfo.all()
      )
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_action_close))
      }
    },
    dismissButton = {
      TextButton(onClick = onOpenWebsite) {
        Text(stringResource(R.string.about_app_open_project_website))
      }
    }
  )
}
