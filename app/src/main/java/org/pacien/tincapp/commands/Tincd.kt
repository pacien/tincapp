package org.pacien.tincapp.commands

import org.pacien.tincapp.context.AppPaths

/**
 * @author pacien
 */
object Tincd {

    fun start(netName: String, deviceFd: Int, ed25519PrivateKeyFd: Int? = null, rsaPrivateKeyFd: Int? = null) {
        Executor.forkExec(Command(AppPaths.tincd().absolutePath)
                .withOption("no-detach")
                .withOption("config", AppPaths.confDir(netName).absolutePath)
                .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)
                .withOption("logfile", AppPaths.logFile(netName).absolutePath)
                .withOption("option", "DeviceType=fd")
                .withOption("option", "Device=" + deviceFd)
                .apply { if (ed25519PrivateKeyFd != null) withOption("option", "Ed25519PrivateKeyFile=/proc/self/fd/$ed25519PrivateKeyFd") }
                .apply { if (rsaPrivateKeyFd != null) withOption("option", "PrivateKeyFile=/proc/self/fd/$rsaPrivateKeyFd") })
    }

}
