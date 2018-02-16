package org.pacien.tincapp.commands

import java8.util.concurrent.CompletableFuture
import org.pacien.tincapp.context.AppPaths

/**
 * @author pacien
 */
object Tinc {

  private fun newCommand(netName: String): Command =
    Command(AppPaths.tinc().absolutePath)
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)

  fun stop(netName: String): CompletableFuture<Unit> =
    Executor.call(newCommand(netName).withArguments("stop"))
      .thenApply { }

  fun pid(netName: String): CompletableFuture<Int> =
    Executor.call(newCommand(netName).withArguments("pid"))
      .thenApply { Integer.parseInt(it.first()) }

  fun dumpNodes(netName: String, reachable: Boolean = false): CompletableFuture<List<String>> =
    Executor.call(
      if (reachable) newCommand(netName).withArguments("dump", "reachable", "nodes")
      else newCommand(netName).withArguments("dump", "nodes"))

  fun info(netName: String, node: String): CompletableFuture<String> =
    Executor.call(newCommand(netName).withArguments("info", node))
      .thenApply<String> { it.joinToString("\n") }

  fun init(netName: String, nodeName: String): CompletableFuture<String> =
    if (netName.isBlank())
      CompletableFuture.failedFuture(IllegalArgumentException("Network name cannot be blank."))
    else
      Executor.call(Command(AppPaths.tinc().absolutePath)
        .withOption("config", AppPaths.confDir(netName).absolutePath)
        .withArguments("init", nodeName))
        .thenApply<String> { it.joinToString("\n") }

  fun join(netName: String, invitationUrl: String): CompletableFuture<String> =
    if (netName.isBlank())
      CompletableFuture.failedFuture(IllegalArgumentException("Network name cannot be blank."))
    else
      Executor.call(Command(AppPaths.tinc().absolutePath)
        .withOption("config", AppPaths.confDir(netName).absolutePath)
        .withArguments("join", invitationUrl))
        .thenApply<String> { it.joinToString("\n") }

}
