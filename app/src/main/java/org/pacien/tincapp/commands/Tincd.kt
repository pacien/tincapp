package org.pacien.tincapp.commands

import org.pacien.tincapp.context.AppPaths
import java.io.IOException

/**
 * @author pacien
 */
object Tincd {

    @Throws(IOException::class)
    fun start(netConf: AppPaths.NetConf, fd: Int) {
        Executor.forkExec(Command(AppPaths.tincd().absolutePath)
                .withOption("no-detach")
                .withOption("config", AppPaths.confDir(netConf).absolutePath)
                .withOption("pidfile", AppPaths.pidFile(netConf).absolutePath)
                .withOption("logfile", AppPaths.logFile(netConf).absolutePath)
                .withOption("option", "DeviceType=fd")
                .withOption("option", "Device=" + fd))
    }

}
