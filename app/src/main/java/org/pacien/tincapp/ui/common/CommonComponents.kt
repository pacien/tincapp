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

package org.pacien.tincapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.pacien.tincapp.ui.theme.ActivityHorizontalMargin
import org.pacien.tincapp.ui.theme.ActivitySectionTitleBottomMargin
import org.pacien.tincapp.ui.theme.ActivitySectionTitleTopMargin
import org.pacien.tincapp.ui.theme.ActivityVerticalMargin
import org.pacien.tincapp.ui.theme.Separator
import org.pacien.tincapp.ui.theme.TextAccent
import org.pacien.tincapp.ui.theme.TextSecondary

@Composable
fun SectionTitle(
  text: String,
  modifier: Modifier = Modifier,
) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    color = TextAccent,
    modifier = modifier
        .fillMaxWidth()
        .padding(
            top = ActivitySectionTitleTopMargin,
            bottom = ActivitySectionTitleBottomMargin,
            start = ActivityVerticalMargin,
            end = ActivityVerticalMargin
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
fun MenuDivider(modifier: Modifier = Modifier) {
  HorizontalDivider(
    modifier = modifier.padding(8.dp),
    thickness = 1.dp,
    color = Separator
  )
}
