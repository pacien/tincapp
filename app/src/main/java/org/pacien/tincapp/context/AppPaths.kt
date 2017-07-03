package org.pacien.tincapp.context

import java.io.File

/**
 * @author pacien
 * *
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
object AppPaths {

    private val TINCD_BIN = "libtincd.so"
    private val TINC_BIN = "libtinc.so"

    private val LOGFILE_FORMAT = "tinc.%s.log"
    private val PIDFILE_FORMAT = "tinc.%s.pid"

    private val NET_CONF_FILE = "network.conf"

    fun cacheDir() = App.getContext().externalCacheDir!!
    fun confDir() = App.getContext().getExternalFilesDir(null)!!
    fun binDir() = File(App.getContext().applicationInfo.nativeLibraryDir)

    fun confDir(netName: String) = File(confDir(), netName)
    fun netConfFile(netName: String) = File(confDir(netName), NET_CONF_FILE)
    fun logFile(netName: String) = File(cacheDir(), String.format(LOGFILE_FORMAT, netName))
    fun pidFile(netName: String) = File(App.getContext().cacheDir, String.format(PIDFILE_FORMAT, netName))

    fun tincd() = File(binDir(), TINCD_BIN)
    fun tinc() = File(binDir(), TINC_BIN)

}
