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

import java.util.concurrent.CompletableFuture
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Java.failedFuture

/**
 * @author euxane
 */
object Tinc {
  private fun newCommand(netName: String): Command =
    Command(AppPaths.tinc().absolutePath)
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)

  fun stop(netName: String): CompletableFuture<Unit> =
    Executor.call(newCommand(netName).withArguments("stop"))
      .thenApply { }

  fun retry(netName: String): CompletableFuture<Unit> =
    Executor.call(newCommand(netName).withArguments("retry"))
      .thenApply { }

  fun pid(netName: String): CompletableFuture<Int> =
    Executor.call(newCommand(netName).withArguments("pid"))
      .thenApply { Integer.parseInt(it.first()) }

  fun dumpNodes(netName: String, reachable: Boolean = false): CompletableFuture<List<String>> =
    Executor.call(
      if (reachable) newCommand(netName).withArguments("dump", "reachable", "nodes")
      else newCommand(netName).withArguments("dump", "nodes"))

  fun dumpSubnets(netName: String): CompletableFuture<List<String>> =
    Executor.call(
      newCommand(netName).withArguments("dump", "subnets"))

  fun dumpGraph(netName: String): CompletableFuture<String> =
    Executor.call(newCommand(netName).withArguments("dump", "graph"))
      .thenApply { it.joinToString("\n") }

  fun info(netName: String, node: String): CompletableFuture<String> =
    Executor.call(newCommand(netName).withArguments("info", node))
      .thenApply { it.joinToString("\n") }

  fun init(netName: String, nodeName: String): CompletableFuture<String> =
    if (netName.isBlank())
      failedFuture(IllegalArgumentException("Network name cannot be blank."))
    else
      Executor.call(Command(AppPaths.tinc().absolutePath)
        .withOption("config", AppPaths.confDir(netName).absolutePath)
        .withArguments("init", nodeName))
        .thenApply { it.joinToString("\n") }

  fun join(netName: String, invitationUrl: String): CompletableFuture<String> =
    if (netName.isBlank())
      failedFuture(IllegalArgumentException("Network name cannot be blank."))
    else
      Executor.call(Command(AppPaths.tinc().absolutePath)
        .withOption("config", AppPaths.confDir(netName).absolutePath)
        .withArguments("join", invitationUrl))
        .thenApply { it.joinToString("\n") }

  fun log(netName: String, level: Int? = null): Process =
    Executor.run(newCommand(netName)
      .withArguments("log")
      .apply { if (level != null) withArguments(level.toString()) })
}
