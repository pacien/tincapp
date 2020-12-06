/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.status.nodes

import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author pacien
 */
data class NodeInfo(val name: String,
                    val id: String,
                    val ip: String,
                    val port: String,
                    val cipher: Int,
                    val digest: Int,
                    val macLength: Int,
                    val compression: Int,
                    val options: Int,
                    val status: Int,
                    val nextHop: String,
                    val via: String,
                    val distance: Int,
                    val pMtu: Int,
                    val minMtu: Int,
                    val maxMtu: Int) {

  // https://github.com/gsliepen/tinc/blob/950bbc8f2f9c580ac85bef7bab9a3ae36ea99c4b/src/info.c#L174
  fun reachabilityText(): String = when {
    ip == "MYSELF" -> RESOURCES.getString(R.string.status_node_reachability_this_node)
    distance == -1 -> RESOURCES.getString(R.string.status_node_reachability_unreachable)
    minMtu > 0 || nextHop == name -> RESOURCES.getString(R.string.status_node_reachability_direct_connection)
    distance > 1 -> RESOURCES.getString(R.string.status_node_reachability_via_format, nextHop)
    else -> RESOURCES.getString(R.string.status_node_reachability_unknown)
  }

  companion object {
    private const val NODE_DUMP_PATTERN_STRING =
      "(\\S+) " +
        "id (\\S+) " +
        "at (\\S+) " +
        "port (\\S+) " +
        "cipher (\\S+) " +
        "digest (\\S+) " +
        "maclength (\\S+) " +
        "compression (\\S+) " +
        "options (\\S+) " +
        "status (\\S+) " +
        "nexthop (\\S+) " +
        "via (\\S+) " +
        "distance (\\S+) " +
        "pmtu (\\S+) \\(min (\\S+) max (\\S+)\\)"

    private val NODE_DUMP_PATTERN by lazy { Pattern.compile(NODE_DUMP_PATTERN_STRING) }
    private val RESOURCES by lazy { App.getResources() }

    fun ofNodeDump(line: String) =
      ofNodeDump(NODE_DUMP_PATTERN.matcher(line).apply { find() })

    private fun ofNodeDump(matcher: Matcher) = NodeInfo(
      name = matcher[1]!!,
      id = matcher[2]!!,
      ip = matcher[3]!!,
      port = matcher[4]!!,
      cipher = matcher[5]!!.toInt(),
      digest = matcher[6]!!.toInt(),
      macLength = matcher[7]!!.toInt(),
      compression = matcher[8]!!.toInt(),
      options = matcher[9]!!.toInt(16),
      status = matcher[10]!!.toInt(16),
      nextHop = matcher[11]!!,
      via = matcher[12]!!,
      distance = matcher[13]!!.toInt(),
      pMtu = matcher[14]!!.toInt(),
      minMtu = matcher[15]!!.toInt(),
      maxMtu = matcher[16]!!.toInt()
    )

    private operator fun Matcher.get(index: Int) = group(index)
  }
}
