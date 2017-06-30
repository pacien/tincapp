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

    fun confDir(ctx: Context): File = ctx.getDir(CONFDIR, Context.MODE_PRIVATE)
    fun confDir(ctx: Context, netName: String): File = File(confDir(ctx), netName)
    fun logDir(ctx: Context): File = createDirIfNotExists(ctx.cacheDir, LOGDIR)
    fun pidDir(ctx: Context): File = createDirIfNotExists(ctx.cacheDir, PIDDIR)
    fun logFile(ctx: Context, netName: String): File = File(logDir(ctx), String.format(LOGFILE_FORMAT, netName))
    fun pidFile(ctx: Context, netName: String): File = File(pidDir(ctx), String.format(PIDFILE_FORMAT, netName))
    fun netConfFile(ctx: Context, netName: String): File = File(confDir(ctx, netName), NET_CONF_FILE)
    fun binDir(ctx: Context): File = File(ctx.applicationInfo.nativeLibraryDir)
    fun tincd(ctx: Context): File = File(binDir(ctx), TINCD_BIN)
    fun tinc(ctx: Context): File = File(binDir(ctx), TINC_BIN)

}
