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

package org.pacien.tincapp.activities.status.subnets

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author pacien
 */
data class SubnetInfo(val ipRange: String, val owner: String) {
  companion object {
    private const val SUBNET_DUMP_PATTERN_STRING = "(\\S+) owner (\\S+)"
    private val SUBNET_DUMP_PATTERN by lazy { Pattern.compile(SUBNET_DUMP_PATTERN_STRING) }

    fun ofSubnetDump(line: String) = ofSubnetDump(SUBNET_DUMP_PATTERN.matcher(line).apply { find() })
    private fun ofSubnetDump(matcher: Matcher) = SubnetInfo(ipRange = matcher[1], owner = matcher[2])
    private operator fun Matcher.get(index: Int) = group(index)
  }
}
