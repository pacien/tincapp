package org.pacien.tincapp.context

import java.io.File
import java.io.Serializable

/**
 * @author pacien
 * *
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
object AppPaths {

    enum class Storage { INTERNAL, EXTERNAL }
    data class NetConf(val storage: Storage, val netName: String) : Serializable

    private val TINCD_BIN = "libtincd.so"
    private val TINC_BIN = "libtinc.so"

    private val LOGFILE_FORMAT = "tinc.%s.log"
    private val PIDFILE_FORMAT = "tinc.%s.pid"

    private val NET_CONF_FILE = "network.conf"

    fun filesDir(storage: Storage): File = when (storage) {
        Storage.INTERNAL -> App.getContext().filesDir
        Storage.EXTERNAL -> App.getContext().getExternalFilesDir(null)
    }

    fun cacheDir(storage: Storage): File = when (storage) {
        Storage.INTERNAL -> App.getContext().cacheDir
        Storage.EXTERNAL -> App.getContext().externalCacheDir
    }

    fun binDir() = File(App.getContext().applicationInfo.nativeLibraryDir)

    fun confDir(storage: Storage) = filesDir(storage)
    fun confDir(netConf: NetConf) = File(confDir(netConf.storage), netConf.netName)

    fun netConfFile(netConf: NetConf) = File(confDir(netConf), NET_CONF_FILE)
    fun logFile(netConf: NetConf) = File(cacheDir(netConf.storage), String.format(LOGFILE_FORMAT, netConf.netName))
    fun pidFile(netConf: NetConf) = File(cacheDir(Storage.INTERNAL), String.format(PIDFILE_FORMAT, netConf.netName))

    fun tincd() = File(binDir(), TINCD_BIN)
    fun tinc() = File(binDir(), TINC_BIN)

}
