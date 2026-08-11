package org.pacien.tincapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppInfo
import org.pacien.tincapp.ui.theme.ActivityHorizontalMargin
import org.pacien.tincapp.ui.theme.ActivityVerticalMargin
import org.pacien.tincapp.ui.theme.Separator
import org.pacien.tincapp.ui.theme.TextAccent
import org.pacien.tincapp.ui.theme.TextSecondary

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = TextAccent,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ActivityHorizontalMargin,
                end = ActivityHorizontalMargin,
                top = ActivityVerticalMargin,
                bottom = 8.dp
            )
    )
}

@Composable
fun ListBlock(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ActivityHorizontalMargin,
                vertical = ActivityVerticalMargin
            )
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
    }
}

@Composable
fun TincDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = Separator
    )
}

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
fun DecryptKeysDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var passphrase by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.decrypt_key_modal_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = passphrase,
                    onValueChange = { passphrase = it },
                    label = { Text(stringResource(R.string.decrypt_key_modal_field_passphrase)) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(passphrase) }) {
                Text(stringResource(R.string.decrypt_key_modal_action_unlock))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.decrypt_key_modal_action_cancel))
            }
        }
    )
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

@Composable
fun GenericActionsMenu(
    actions: GenericActions,
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
            TincDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about_app_menu_manual)) },
                onClick = {
                    showMenu = false
                    actions.onManual(null)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.about_app_menu_donate)) },
                onClick = {
                    showMenu = false
                    actions.onManual("#donations")
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

data class GenericActions(
    val onOpenNetworksDir: () -> Unit,
    val onLogDir: () -> Unit,
    val onManual: (String?) -> Unit,
    val onAbout: () -> Unit
)
