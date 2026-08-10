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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.status.networkinfo.VpnInterfaceConfigurationFormatter
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import androidx.compose.ui.platform.LocalResources

@Composable
fun NetworkInfoScreen(
    vpnInterfaceConfiguration: VpnInterfaceConfiguration,
    modifier: Modifier = Modifier
) {
    val formatter = VpnInterfaceConfigurationFormatter
    val resources = LocalResources.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        ListBlock(
            title = stringResource(R.string.status_network_info_ip_addresses_label),
            content = formatter.formatList(vpnInterfaceConfiguration.addresses, resources)
        )

        ListBlock(
            title = stringResource(R.string.status_network_info_routes_label),
            content = formatter.formatList(vpnInterfaceConfiguration.routes, resources)
        )

        ListBlock(
            title = stringResource(R.string.status_network_info_dns_servers_label),
            content = formatter.formatList(vpnInterfaceConfiguration.dnsServers, resources)
        )

        ListBlock(
            title = stringResource(R.string.status_network_info_search_domains_label),
            content = formatter.formatList(vpnInterfaceConfiguration.searchDomains, resources)
        )

        if (vpnInterfaceConfiguration.allowedApplications.isNotEmpty()) {
            ListBlock(
                title = stringResource(R.string.status_network_info_allowed_applications_label),
                content = formatter.formatList(vpnInterfaceConfiguration.allowedApplications, resources)
            )
        }

        if (vpnInterfaceConfiguration.disallowedApplications.isNotEmpty()) {
            ListBlock(
                title = stringResource(R.string.status_network_info_disallowed_applications_label),
                content = formatter.formatList(vpnInterfaceConfiguration.disallowedApplications, resources)
            )
        }

        ListBlock(
            title = stringResource(R.string.status_network_info_allow_bypass_label),
            content = stringResource(
                if (vpnInterfaceConfiguration.allowBypass) R.string.status_network_info_value_yes
                else R.string.status_network_info_value_no
            )
        )
    }
}
