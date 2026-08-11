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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.status.networkinfo.VpnInterfaceConfigurationFormatter
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.activities.status.subnets.SubnetInfo
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.ui.components.SectionTitle

@Composable
fun NetworkInfoScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    nodes: List<NodeInfo>,
    onNodeClick: (NodeInfo) -> Unit,
    subnets: List<SubnetInfo>,
    modifier: Modifier = Modifier
) {
    val formatter = VpnInterfaceConfigurationFormatter
    val resources = LocalResources.current

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item {
            SectionTitle(text = stringResource(R.string.status_activity_title_network_info))
        }

        item {
            ListBlock(
                title = stringResource(R.string.status_network_info_ip_addresses_label),
                content = formatter.formatList(vpnInterfaceConfiguration.addresses, resources)
            )
        }

        item {
            ListBlock(
                title = stringResource(R.string.status_network_info_routes_label),
                content = formatter.formatList(vpnInterfaceConfiguration.routes, resources)
            )
        }

        item {
            ListBlock(
                title = stringResource(R.string.status_network_info_dns_servers_label),
                content = formatter.formatList(vpnInterfaceConfiguration.dnsServers, resources)
            )
        }

        item {
            ListBlock(
                title = stringResource(R.string.status_network_info_search_domains_label),
                content = formatter.formatList(vpnInterfaceConfiguration.searchDomains, resources)
            )
        }

        if (vpnInterfaceConfiguration.allowedApplications.isNotEmpty()) {
            item {
                ListBlock(
                    title = stringResource(R.string.status_network_info_allowed_applications_label),
                    content = formatter.formatList(vpnInterfaceConfiguration.allowedApplications, resources)
                )
            }
        }

        if (vpnInterfaceConfiguration.disallowedApplications.isNotEmpty()) {
            item {
                ListBlock(
                    title = stringResource(R.string.status_network_info_disallowed_applications_label),
                    content = formatter.formatList(vpnInterfaceConfiguration.disallowedApplications, resources)
                )
            }
        }

        item {
            ListBlock(
                title = stringResource(R.string.status_network_info_allow_bypass_label),
                content = stringResource(
                    if (vpnInterfaceConfiguration.allowBypass) R.string.status_network_info_value_yes
                    else R.string.status_network_info_value_no
                )
            )
        }

        item {
            SectionTitle(text = stringResource(R.string.status_activity_title_node_list))
        }

        if (nodes.isEmpty()) {
            item {
                EmptyPlaceholder(text = stringResource(R.string.status_node_list_empty_placeholder_text))
            }
        } else {
            items(nodes) { node ->
                NodeListItem(
                    node = node,
                    reachabilityText = node.reachabilityText(resources),
                    onClick = { onNodeClick(node) }
                )
            }
        }

        item {
            SectionTitle(text = stringResource(R.string.status_activity_title_subnet_list))
        }

        if (subnets.isEmpty()) {
            item {
                EmptyPlaceholder(text = stringResource(R.string.status_subnet_list_empty_placeholder_text))
            }
        } else {
            items(subnets) { subnet ->
                SubnetListItem(subnet = subnet)
            }
        }
    }
}
