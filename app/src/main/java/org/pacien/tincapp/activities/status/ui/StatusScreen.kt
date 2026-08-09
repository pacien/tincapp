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

package org.pacien.tincapp.activities.status.ui

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.MutableLiveData
import org.pacien.tincapp.activities.common.SelfRefreshingLiveData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import androidx.compose.ui.tooling.preview.Preview
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.activities.status.subnets.SubnetInfo
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.components.GenericActionsMenu
import org.pacien.tincapp.ui.theme.BackgroundSecondary
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TincTheme
import org.pacien.tincapp.ui.theme.TextTitle

enum class StatusTab(val titleRes: Int) {
    NETWORK_INFO(R.string.status_activity_title_network_info),
    NODES(R.string.status_activity_title_node_list),
    SUBNETS(R.string.status_activity_title_subnet_list),
    LOGS(R.string.status_activity_title_log_view)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    nodeListLiveData: LiveData<List<NodeInfo>>,
    subnetListLiveData: LiveData<List<SubnetInfo>>,
    logLiveData: LiveData<List<String>>,
    onNodeClick: (NodeInfo) -> Unit,
    netName: String?,
    onDisconnectClick: () -> Unit,
    onAboutClick: () -> Unit,
    onOpenNetworksDirClick: () -> Unit,
    onLogDirClick: () -> Unit,
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
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text =
                                    if (netName != null)
                                        stringResource(R.string.status_activity_state_connected_to_format, netName)
                                    else
                                        stringResource(R.string.status_activity_state_disconnected),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onDisconnectClick) {
                            Icon(
                                imageVector = Icons.Default.LinkOff,
                                contentDescription = stringResource(R.string.status_activity_menu_disconnect),
                                tint = TextTitle
                            )
                        }
                        GenericActionsMenu(
                            onOpenNetworksDirClick = onOpenNetworksDirClick,
                            onLogDirClick = onLogDirClick,
                            onAboutClick = onAboutClick,
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
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { pageIndex ->
            val isCurrentPage = pagerState.currentPage == pageIndex
            when (tabs[pageIndex]) {
                StatusTab.NETWORK_INFO -> NetworkInfoScreen(
                    vpnInterfaceConfiguration = vpnInterfaceConfiguration
                )
                StatusTab.NODES -> {
                    val nodes by nodeListLiveData.observeAsStateActive(isCurrentPage, emptyList())
                    NodeListScreen(
                        nodes = nodes,
                        onNodeClick = onNodeClick
                    )
                }
                StatusTab.SUBNETS -> {
                    val subnets by subnetListLiveData.observeAsStateActive(isCurrentPage, emptyList())
                    SubnetListScreen(
                        subnets = subnets
                    )
                }
                StatusTab.LOGS -> {
                    val logLines by logLiveData.observeAsStateActive(isCurrentPage, emptyList())
                    LogView(
                        logText = logLines.joinToString("\n")
                    )
                }
            }
        }
    }
}

/**
 * Observe a LiveData as state only when [active] is true.
 *
 * When [active] becomes false, the state will keep its last value.
 * This is useful for stopping refreshes of [SelfRefreshingLiveData] when not in focus.
 */
@Composable
fun <T> LiveData<T>.observeAsStateActive(active: Boolean, initial: T): State<T> {
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this, active) {
        if (active) {
            val observer = Observer<T> { state.value = it }
            observeForever(observer)
            onDispose {
                removeObserver(observer)
            }
        } else {
            onDispose {}
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
        Text(
            text = logText,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    TincTheme {
        StatusScreen(
            vpnInterfaceConfiguration = VpnInterfaceConfiguration(),
            nodeListLiveData = MutableLiveData(listOf(
                NodeInfo("node1", "id1", "1.2.3.4", "1234", 0, 0, 0, 0, 0, 0, "node1", "node1", 1, 1500, 1500, 1500),
                NodeInfo("node2", "id2", "5.6.7.8", "5678", 0, 0, 0, 0, 0, 0, "node1", "node1", 2, 1500, 1500, 1500)
            )),
            subnetListLiveData = MutableLiveData(listOf(
                SubnetInfo("10.0.0.0/24", "node1"),
                SubnetInfo("10.0.1.0/24", "node2")
            )),
            logLiveData = MutableLiveData(listOf("Log line 1", "Log line 2")),
            onNodeClick = {},
            netName = "test-net",
            onDisconnectClick = {},
            onAboutClick = {},
            onOpenNetworksDirClick = {},
            onLogDirClick = {}
        )
    }
}
