/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2018 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.commands

import org.pacien.tincapp.context.AppPaths

/**
 * @author euxane
 */
object Tinc {
  private fun newCommand(netName: String): Command =
    Command(AppPaths.tinc().absolutePath)
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)

  suspend fun stop(netName: String) {
    Executor.callSuspend(newCommand(netName).withArguments("stop"))
  }

  suspend fun retry(netName: String) {
    Executor.callSuspend(newCommand(netName).withArguments("retry"))
  }

  suspend fun pid(netName: String): Int =
    Executor.callSuspend(newCommand(netName).withArguments("pid")).let {
      Integer.parseInt(it.first())
    }

  suspend fun dumpNodes(netName: String, reachable: Boolean = false): List<String> =
    Executor.callSuspend(
      if (reachable) newCommand(netName).withArguments("dump", "reachable", "nodes")
      else newCommand(netName).withArguments("dump", "nodes"))

  suspend fun dumpSubnets(netName: String): List<String> =
    Executor.callSuspend(
      newCommand(netName).withArguments("dump", "subnets"))

  suspend fun dumpGraph(netName: String): String =
    Executor.callSuspend(newCommand(netName).withArguments("dump", "graph"))
      .joinToString("\n")

  suspend fun info(netName: String, node: String): String =
    Executor.callSuspend(newCommand(netName).withArguments("info", node))
      .joinToString("\n")

  suspend fun init(netName: String, nodeName: String): String {
    if (netName.isBlank()) throw IllegalArgumentException("Network name cannot be blank.")
    return Executor.callSuspend(Command(AppPaths.tinc().absolutePath)
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withArguments("init", nodeName))
      .joinToString("\n")
  }

  suspend fun join(netName: String, invitationUrl: String): String {
    if (netName.isBlank()) throw IllegalArgumentException("Network name cannot be blank.")
    return Executor.callSuspend(Command(AppPaths.tinc().absolutePath)
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withArguments("join", invitationUrl))
      .joinToString("\n")
  }

  fun log(netName: String, level: Int? = null): Process =
    Executor.run(newCommand(netName)
      .withArguments("log")
      .apply { if (level != null) withArguments(level.toString()) })
}
