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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.pacien.tincapp.activities.status.nodes.NodeInfo
import org.pacien.tincapp.activities.status.subnets.SubnetInfo
import org.pacien.tincapp.ui.theme.ActivityHorizontalMargin
import org.pacien.tincapp.ui.theme.ActivityVerticalMargin
import org.pacien.tincapp.ui.theme.InfoListItemVerticalMargin
import org.pacien.tincapp.ui.theme.TextSecondary

@Composable
fun ListBlock(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ActivityHorizontalMargin,
                vertical = InfoListItemVerticalMargin
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun EmptyPlaceholder(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
        color = TextSecondary,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ActivityHorizontalMargin,
                vertical = ActivityVerticalMargin
            )
    )
}

@Composable
fun NodeListItem(
    node: NodeInfo,
    reachabilityText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ActivityHorizontalMargin,
                vertical = InfoListItemVerticalMargin
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

@Composable
fun SubnetListItem(
    subnet: SubnetInfo,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ActivityHorizontalMargin,
                vertical = InfoListItemVerticalMargin
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subnet.ipRange,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = subnet.owner,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
