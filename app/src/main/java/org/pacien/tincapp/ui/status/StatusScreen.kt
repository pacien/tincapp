/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2025 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import org.pacien.tincapp.ui.status.nodes.NodeInfo
import org.pacien.tincapp.ui.status.subnets.SubnetInfo
import org.pacien.tincapp.config.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.common.AppStatusHeader
import org.pacien.tincapp.ui.common.AppActions
import org.pacien.tincapp.ui.common.AppActionsMenu
import org.pacien.tincapp.ui.status.topograph.GraphView
import org.pacien.tincapp.ui.status.networkinfo.NetworkInfoScreen
import org.pacien.tincapp.ui.theme.BackgroundSecondary
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TextTitle
import org.pacien.tincapp.ui.theme.TincTheme
import org.pacien.tincapp.repository.VpnState

enum class StatusTab(val titleRes: Int) {
    NETWORK_INFO(R.string.status_activity_title_network_info),
    TOPOLOGY(R.string.status_activity_title_topology),
    LOGS(R.string.status_activity_title_log_view)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    nodeListFlow: Flow<List<NodeInfo>>,
    subnetListFlow: Flow<List<SubnetInfo>>,
    graphFlow: Flow<String>,
    logFlow: Flow<List<String>>,
    onNodeClick: (NodeInfo) -> Unit,
    onNodeNameClick: (String) -> Unit,
    vpnState: VpnState,
    onDisconnectClick: () -> Unit,
    appActions: AppActions,
    modifier: Modifier = Modifier
) {
    val tabs = StatusTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        AppStatusHeader(vpnState)
                    },
                    actions = {
                        IconButton(onClick = onDisconnectClick) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = stringResource(R.string.status_activity_menu_disconnect),
                                tint = TextTitle
                            )
                        }
                        AppActionsMenu(
                            actions = appActions,
                            tint = TextTitle
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HeaderPrimary,
                        titleContentColor = TextTitle,
                        actionIconContentColor = TextTitle
                    )
                )
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = BackgroundSecondary
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(text = stringResource(tab.titleRes))
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            userScrollEnabled = false,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            val isCurrentPage = pagerState.currentPage == pageIndex
            when (tabs[pageIndex]) {
                StatusTab.NETWORK_INFO -> {
                    val nodes by nodeListFlow.collectAsStateActive(isCurrentPage, emptyList())
                    val subnets by subnetListFlow.collectAsStateActive(isCurrentPage, emptyList())
                    NetworkInfoScreen(
                        vpnInterfaceConfiguration = vpnInterfaceConfiguration,
                        nodes = nodes,
                        onNodeClick = onNodeClick,
                        subnets = subnets
                    )
                }
                StatusTab.TOPOLOGY -> {
                    val dotString by graphFlow.collectAsStateActive(isCurrentPage, "")
                    GraphView(
                        dotString = dotString,
                        onNodeClick = onNodeNameClick
                    )
                }
                StatusTab.LOGS -> {
                    val logLines by logFlow.collectAsStateActive(isCurrentPage, emptyList())
                    LogView(
                        logText = logLines.joinToString("\n")
                    )
                }
            }
        }
    }
}

/**
 * Collect a Flow as state only when [active] is true.
 *
 * When [active] becomes false, the state will keep its last value.
 * This is useful for stopping refreshes of self-refreshing Flows when not in focus.
 */
@Composable
fun <T> Flow<T>.collectAsStateActive(active: Boolean, initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, active, lifecycleOwner) {
        if (active) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collect { value ->
                    state.value = value
                }
            }
        }
    }
    return state
}

@Composable
fun LogView(
    logText: String,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to bottom when log text changes
    LaunchedEffect(logText) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        if (logText.isEmpty()) {
            EmptyPlaceholder(text = stringResource(R.string.status_log_empty_placeholder))
        } else {
            SelectionContainer {
                Text(
                    text = logText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    TincTheme {
        StatusScreen(
            vpnInterfaceConfiguration = VpnInterfaceConfiguration(),
            nodeListFlow = flowOf(listOf(
                NodeInfo("node1", "id1", "1.2.3.4", "1234", 0, 0, 0, 0, 0, 0, "node1", "node1", 1, 1500, 1500, 1500),
                NodeInfo("node2", "id2", "5.6.7.8", "5678", 0, 0, 0, 0, 0, 0, "node1", "node1", 2, 1500, 1500, 1500)
            )),
            subnetListFlow = flowOf(listOf(
                SubnetInfo("10.0.0.0/24", "node1"),
                SubnetInfo("10.0.1.0/24", "node2")
            )),
            logFlow = flowOf(listOf("Log line 1", "Log line 2")),
            graphFlow = flowOf(""),
            onNodeClick = {},
            onNodeNameClick = {},
            vpnState = VpnState.Connected("test-net"),
            onDisconnectClick = {},
            appActions = AppActions({}, {}, {}, {})
        )
    }
}
