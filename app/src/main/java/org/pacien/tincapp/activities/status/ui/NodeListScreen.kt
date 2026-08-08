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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.ui.theme.Separator
import org.pacien.tincapp.ui.theme.TextSecondary

@Composable
fun NodeListScreen(
    nodes: List<NodeInfo>,
    onNodeClick: (NodeInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val resources = LocalContext.current.resources
    if (nodes.isEmpty()) {
        EmptyPlaceholder(text = stringResource(R.string.status_node_list_empty_placeholder_text))
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(nodes) { node ->
                NodeListItem(
                    node = node,
                    reachabilityText = node.reachabilityText(resources),
                    onClick = { onNodeClick(node) }
                )
                HorizontalDivider(color = Separator)
            }
        }
    }
}

@Composable
private fun NodeListItem(
    node: NodeInfo,
    reachabilityText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = dimensionResource(R.dimen.activity_horizontal_margin),
                vertical = dimensionResource(R.dimen.activity_vertical_margin)
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = node.name,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = reachabilityText,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
