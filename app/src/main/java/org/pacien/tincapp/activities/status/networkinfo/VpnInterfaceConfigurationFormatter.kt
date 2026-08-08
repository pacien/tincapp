/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2020 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.activities.status.networkinfo

import android.content.res.Resources
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.data.CidrAddress

/**
 * @author euxane
 */
object VpnInterfaceConfigurationFormatter {
  fun formatList(list: List<Any>?, resources: Resources = App.getResources()) = when {
    !list.isNullOrEmpty() -> list.joinToString("\n", transform = this::formatListElement)
    else -> resources.getString(R.string.status_network_info_value_none)
  }

  private fun formatListElement(element: Any) = when (element) {
    is CidrAddress -> element.toSlashSeparated()
    is String -> element
    else -> element.toString()
  }
}
