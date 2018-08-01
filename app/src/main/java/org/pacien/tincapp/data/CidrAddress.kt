/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.data

import org.apache.commons.configuration2.ex.ConversionException

/**
 * @author pacien
 */
data class CidrAddress(val address: String, val prefix: Int) {
  companion object {
    private val SEPARATOR = "/"

    fun fromSlashSeparated(s: String) = try {
      CidrAddress(s.substringBefore(SEPARATOR), Integer.parseInt(s.substringAfter(SEPARATOR)))
    } catch (e: Exception) {
      throw ConversionException(e.message, e)
    }
  }

  fun toSlashSeparated() = address + SEPARATOR + prefix
  override fun toString(): String = "$address/$prefix"
}
