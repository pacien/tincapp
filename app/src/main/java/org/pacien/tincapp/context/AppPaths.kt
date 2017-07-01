package org.pacien.tincapp.context

import android.content.Context

import java.io.File

/**
 * @author pacien
 * *
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
object AppPaths {

    private val CONFDIR = "conf"
    private val LOGDIR = "log"
    private val PIDDIR = "pid"

    private val TINCD_BIN = "libtincd.so"
    private val TINC_BIN = "libtinc.so"

    private val LOGFILE_FORMAT = "tinc.%s.log"
    private val PIDFILE_FORMAT = "tinc.%s.pid"

    private val NET_CONF_FILE = "network.conf"

    private fun createDirIfNotExists(basePath: File, newDir: String): File {
        val f = File(basePath, newDir)
        f.mkdirs()
        return f
    }

    fun confDir(): File = App.getContext().getDir(CONFDIR, Context.MODE_PRIVATE)
    fun confDir(netName: String): File = File(confDir(), netName)
    fun logDir(): File = createDirIfNotExists(App.getContext().cacheDir, LOGDIR)
    fun pidDir(): File = createDirIfNotExists(App.getContext().cacheDir, PIDDIR)
    fun logFile(netName: String): File = File(logDir(), String.format(LOGFILE_FORMAT, netName))
    fun pidFile(netName: String): File = File(pidDir(), String.format(PIDFILE_FORMAT, netName))
    fun netConfFile(netName: String): File = File(confDir(netName), NET_CONF_FILE)
    fun binDir(): File = File(App.getContext().applicationInfo.nativeLibraryDir)
    fun tincd(): File = File(binDir(), TINCD_BIN)
    fun tinc(): File = File(binDir(), TINC_BIN)

}
