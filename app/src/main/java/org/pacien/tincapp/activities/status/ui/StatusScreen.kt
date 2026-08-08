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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import androidx.compose.ui.tooling.preview.Preview
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.activities.status.subnets.SubnetInfo
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.theme.BackgroundSecondary
import org.pacien.tincapp.ui.theme.HeaderPrimary
import org.pacien.tincapp.ui.theme.TincTheme
import org.pacien.tincapp.ui.theme.TextTitle

enum class StatusTab(val titleRes: Int) {
    NETWORK_INFO(R.string.status_activity_title_network_info),
    NODES(R.string.status_activity_title_node_list),
    SUBNETS(R.string.status_activity_title_subnet_list)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    nodes: List<NodeInfo>,
    subnets: List<SubnetInfo>,
    onNodeClick: (NodeInfo) -> Unit,
    netName: String?,
    onDisconnectClick: () -> Unit,
    onShowLogClick: () -> Unit,
    onAboutClick: () -> Unit,
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
                            netName?.let {
                                Text(
                                    text = stringResource(R.string.status_activity_state_connected_to_format, it),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = onDisconnectClick) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.ic_link_off_primary_24dp),
                                contentDescription = stringResource(R.string.status_activity_menu_disconnect),
                                tint = TextTitle
                            )
                        }
                        IconButton(onClick = onShowLogClick) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = stringResource(R.string.status_activity_menu_show_log),
                                tint = TextTitle
                            )
                        }
                        IconButton(onClick = onAboutClick) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = stringResource(R.string.about_app_menu_about),
                                tint = TextTitle
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = HeaderPrimary,
                        titleContentColor = TextTitle,
                        actionIconContentColor = TextTitle
                    )
                )
                TabRow(
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
            when (tabs[pageIndex]) {
                StatusTab.NETWORK_INFO -> NetworkInfoScreen(
                    vpnInterfaceConfiguration = vpnInterfaceConfiguration
                )
                StatusTab.NODES -> NodeListScreen(
                    nodes = nodes,
                    onNodeClick = onNodeClick
                )
                StatusTab.SUBNETS -> SubnetListScreen(
                    subnets = subnets
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
            nodes = listOf(
                NodeInfo("node1", "id1", "1.2.3.4", "1234", 0, 0, 0, 0, 0, 0, "node1", "node1", 1, 1500, 1500, 1500),
                NodeInfo("node2", "id2", "5.6.7.8", "5678", 0, 0, 0, 0, 0, 0, "node1", "node1", 2, 1500, 1500, 1500)
            ),
            subnets = listOf(
                SubnetInfo("10.0.0.0/24", "node1"),
                SubnetInfo("10.0.1.0/24", "node2")
            ),
            onNodeClick = {},
            netName = "test-net",
            onDisconnectClick = {},
            onShowLogClick = {},
            onAboutClick = {}
        )
    }
}
