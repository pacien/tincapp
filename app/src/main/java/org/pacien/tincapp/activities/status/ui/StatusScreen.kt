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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import androidx.compose.ui.tooling.preview.Preview
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.activities.status.subnets.SubnetInfo
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.theme.BackgroundSecondary
import org.pacien.tincapp.ui.theme.TincTheme

enum class StatusTab(val titleRes: Int) {
    NETWORK_INFO(R.string.status_activity_title_network_info),
    NODES(R.string.status_activity_title_node_list),
    SUBNETS(R.string.status_activity_title_subnet_list)
}

@Composable
fun StatusScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    nodes: List<NodeInfo>,
    subnets: List<SubnetInfo>,
    onNodeClick: (NodeInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = StatusTab.entries
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
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
            onNodeClick = {}
        )
    }
}
