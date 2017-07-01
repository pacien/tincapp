package org.pacien.tincapp.commands

import org.pacien.tincapp.context.AppPaths
import java.io.IOException

/**
 * @author pacien
 */
object Tinc {

    private fun newCommand(netName: String): Command =
            Command(AppPaths.tinc().absolutePath)
                    .withOption("config", AppPaths.confDir(netName).absolutePath)
                    .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)

    // independently runnable commands

    @Throws(IOException::class)
    fun network(): List<String> =
            Executor.call(Command(AppPaths.tinc().absolutePath)
                    .withOption("config", AppPaths.confDir().absolutePath)
                    .withArguments("network"))

    @Throws(IOException::class)
    fun fsck(netName: String, fix: Boolean): List<String> {
        var cmd = newCommand(netName).withArguments("fsck")
        if (fix) cmd = cmd.withOption("force")
        return Executor.call(cmd)
    }

    // commands requiring a running tinc daemon

    @Throws(IOException::class)
    fun stop(netName: String) {
        Executor.call(newCommand(netName).withArguments("stop"))
    }

    @Throws(IOException::class)
    fun dumpNodes(netName: String, reachable: Boolean): List<String> =
            Executor.call(
                    if (reachable) newCommand(netName).withArguments("dump", "reachable", "nodes")
                    else newCommand(netName).withArguments("dump", "nodes"))

    @Throws(IOException::class)
    fun info(netName: String, node: String): String =
            Executor.call(newCommand(netName).withArguments("info", node)).joinToString("\n")

}
