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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.theme.TextSecondary

@Composable
fun ProgressDialog(text: String) {
  Dialog(onDismissRequest = {}) {
    Surface(
      shape = MaterialTheme.shapes.medium,
      color = MaterialTheme.colorScheme.surface,
      tonalElevation = 6.dp
    ) {
      Row(
        modifier = Modifier
          .padding(20.dp)
          .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = text, color = TextSecondary)
      }
    }
  }
}

@Composable
fun ErrorDialog(
  message: String,
  docTopic: String? = null,
  onDismiss: () -> Unit,
  onOpenManual: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.generic_title_error)) },
    text = { Text(message) },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_action_dismiss))
      }
    },
    dismissButton = {
      if (docTopic != null) {
        TextButton(onClick = { onOpenManual(docTopic) }) {
          Text(stringResource(R.string.notification_error_action_help))
        }
      }
    }
  )
}

@Composable
fun WarningDialog(
  message: String,
  docTopic: String? = null,
  onDismiss: () -> Unit,
  onContinue: () -> Unit,
  onOpenManual: (String) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.generic_title_warning)) },
    text = { Text(message) },
    confirmButton = {
      TextButton(onClick = onContinue) {
        Text(stringResource(R.string.generic_action_continue))
      }
    },
    dismissButton = {
      Row {
        if (docTopic != null) {
          TextButton(onClick = { onOpenManual(docTopic) }) {
            Text(stringResource(R.string.notification_error_action_help))
          }
        }
        TextButton(onClick = onDismiss) {
          Text(stringResource(R.string.generic_action_cancel))
        }
      }
    }
  )
}

@Composable
fun CrashDialog(
  message: String,
  onDismiss: () -> Unit,
  onSendReport: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(stringResource(R.string.crash_modal_title)) },
    text = { Text(message) },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(R.string.generic_action_dismiss))
      }
    },
    dismissButton = {
      TextButton(onClick = onSendReport) {
        Text(stringResource(R.string.crash_modal_action_send_report))
      }
    }
  )
}
