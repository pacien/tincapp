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

package org.pacien.tincapp.activities.viewlog

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PauseCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.theme.TincTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewLogScreen(
    logText: String,
    logLevelText: String,
    isLoggingEnabled: Boolean,
    onBackClick: () -> Unit,
    onToggleLogging: () -> Unit,
    onShareClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom when log text changes and logging is enabled
    LaunchedEffect(logText) {
        if (isLoggingEnabled) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.status_activity_menu_show_log))
                        Text(
                            text = if (isLoggingEnabled) {
                                stringResource(R.string.log_view_state_level_format, logLevelText)
                            } else {
                                stringResource(R.string.log_view_state_paused)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleLogging) {
                        Icon(
                            imageVector = if (isLoggingEnabled) {
                                Icons.Default.PauseCircleOutline
                            } else {
                                Icons.Default.PauseCircleFilled
                            },
                            contentDescription = stringResource(R.string.log_view_menu_toggle_logging)
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.log_view_menu_share_log)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = logText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ViewLogScreenPreview() {
    TincTheme {
        ViewLogScreen(
            logText = "Log line 1\n\nLog line 2\n\nLog line 3",
            logLevelText = "DEBUG",
            isLoggingEnabled = true,
            onBackClick = {},
            onToggleLogging = {},
            onShareClick = {}
        )
    }
}
