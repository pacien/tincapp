package org.pacien.tincapp.commands

import org.pacien.tincapp.context.AppPaths
import java.io.IOException

/**
 * @author pacien
 */
object Tinc {

    private fun newCommand(netConf: AppPaths.NetConf): Command =
            Command(AppPaths.tinc().absolutePath)
                    .withOption("config", AppPaths.confDir(netConf).absolutePath)
                    .withOption("pidfile", AppPaths.pidFile(netConf).absolutePath)

    // independently runnable commands

    @Throws(IOException::class)
    fun fsck(netConf: AppPaths.NetConf, fix: Boolean): List<String> {
        var cmd = newCommand(netConf).withArguments("fsck")
        if (fix) cmd = cmd.withOption("force")
        return Executor.call(cmd)
    }

    // commands requiring a running tinc daemon

    @Throws(IOException::class)
    fun stop(netConf: AppPaths.NetConf) {
        Executor.call(newCommand(netConf).withArguments("stop"))
    }

    @Throws(IOException::class)
    fun dumpNodes(netConf: AppPaths.NetConf, reachable: Boolean): List<String> =
            Executor.call(
                    if (reachable) newCommand(netConf).withArguments("dump", "reachable", "nodes")
                    else newCommand(netConf).withArguments("dump", "nodes"))

    @Throws(IOException::class)
    fun info(netConf: AppPaths.NetConf, node: String): String =
            Executor.call(newCommand(netConf).withArguments("info", node)).joinToString("\n")

}
