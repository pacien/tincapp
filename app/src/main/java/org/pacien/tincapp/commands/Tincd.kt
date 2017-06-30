package org.pacien.tincapp.commands

import android.content.Context

import org.pacien.tincapp.context.AppPaths

import java.io.IOException

/**
 * @author pacien
 */
object Tincd {

    @Throws(IOException::class)
    fun start(ctx: Context, netName: String, fd: Int) {
        Executor.forkExec(Command(AppPaths.tincd(ctx).absolutePath)
                .withOption("no-detach")
                .withOption("config", AppPaths.confDir(ctx, netName).absolutePath)
                .withOption("pidfile", AppPaths.pidFile(ctx, netName).absolutePath)
                .withOption("logfile", AppPaths.logFile(ctx, netName).absolutePath)
                .withOption("option", "DeviceType=fd")
                .withOption("option", "Device=" + fd))
    }

}
