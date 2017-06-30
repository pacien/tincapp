package org.pacien.tincapp.commands

import android.content.Context
import org.pacien.tincapp.context.AppPaths
import java.io.IOException

/**
 * @author pacien
 */
object Tinc {

    private fun newCommand(ctx: Context, netName: String): Command =
            Command(AppPaths.tinc(ctx).absolutePath)
                    .withOption("config", AppPaths.confDir(ctx, netName).absolutePath)
                    .withOption("pidfile", AppPaths.pidFile(ctx, netName).absolutePath)

    // independently runnable commands

    @Throws(IOException::class)
    fun network(ctx: Context): List<String> =
            Executor.call(Command(AppPaths.tinc(ctx).absolutePath)
                    .withOption("config", AppPaths.confDir(ctx).absolutePath)
                    .withArguments("network"))

    @Throws(IOException::class)
    fun fsck(ctx: Context, netName: String, fix: Boolean): List<String> {
        var cmd = newCommand(ctx, netName).withArguments("fsck")
        if (fix) cmd = cmd.withOption("force")
        return Executor.call(cmd)
    }

    // commands requiring a running tinc daemon

    @Throws(IOException::class)
    fun stop(ctx: Context, netName: String) {
        Executor.call(newCommand(ctx, netName).withArguments("stop"))
    }

    @Throws(IOException::class)
    fun dumpNodes(ctx: Context, netName: String, reachable: Boolean): List<String> =
            Executor.call(
                    if (reachable) newCommand(ctx, netName).withArguments("dump", "reachable", "nodes")
                    else newCommand(ctx, netName).withArguments("dump", "nodes"))

    @Throws(IOException::class)
    fun info(ctx: Context, netName: String, node: String): String =
            Executor.call(newCommand(ctx, netName).withArguments("info", node)).joinToString("\n")

}
