/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
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

import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.data.CidrAddress

/**
 * @author pacien
 */
object VpnInterfaceConfigurationFormatter {
  private val resources by lazy { App.getResources() }

  fun formatList(list: List<Any>?) = when {
    list != null && list.isNotEmpty() -> list.joinToString("\n", transform = this::formatListElement)
    else -> resources.getString(R.string.status_network_info_value_none)!!
  }

  private fun formatListElement(element: Any) = when (element) {
    is CidrAddress -> element.toSlashSeparated()
    is String -> element
    else -> element.toString()
  }
}
