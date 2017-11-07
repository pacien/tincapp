package org.pacien.tincapp.context

import android.os.Environment
import java.io.File
import java.io.FileNotFoundException

/**
 * @author pacien
 *
 * @implNote Logs and PID files are stored in the cache directory for easy clean up.
 */
object AppPaths {

    private val TINCD_BIN = "libtincd.so"
    private val TINC_BIN = "libtinc.so"

    private val LOGFILE_FORMAT = "tinc.%s.log"
    private val PIDFILE_FORMAT = "tinc.%s.pid"

    private val NET_CONF_FILE = "network.conf"
    private val NET_TINC_CONF_FILE = "tinc.conf"
    private val NET_HOSTS_DIR = "hosts"
    private val NET_INVITATION_FILE = "invitation-data"
    private val NET_DEFAULT_ED25519_PRIVATE_KEY_FILE = "ed25519_key.priv"
    private val NET_DEFAULT_RSA_PRIVATE_KEY_FILE = "rsa_key.priv"

    fun storageAvailable() =
            Environment.getExternalStorageState().let { it == Environment.MEDIA_MOUNTED && it != Environment.MEDIA_MOUNTED_READ_ONLY }

    fun cacheDir() = App.getContext().externalCacheDir
    fun confDir() = App.getContext().getExternalFilesDir(null)
    fun binDir() = File(App.getContext().applicationInfo.nativeLibraryDir)

    fun confDir(netName: String) = File(confDir(), netName)
    fun hostsDir(netName: String) = File(confDir(netName), NET_HOSTS_DIR)
    fun netConfFile(netName: String) = File(confDir(netName), NET_CONF_FILE)
    fun tincConfFile(netName: String) = File(confDir(netName), NET_TINC_CONF_FILE)
    fun invitationFile(netName: String) = File(confDir(netName), NET_INVITATION_FILE)
    fun logFile(netName: String) = File(cacheDir(), String.format(LOGFILE_FORMAT, netName))
    fun pidFile(netName: String) = File(App.getContext().cacheDir, String.format(PIDFILE_FORMAT, netName))

    fun existing(f: File) = f.apply { if (!exists()) throw FileNotFoundException(f.absolutePath) }

    fun defaultEd25519PrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_ED25519_PRIVATE_KEY_FILE)
    fun defaultRsaPrivateKeyFile(netName: String) = File(confDir(netName), NET_DEFAULT_RSA_PRIVATE_KEY_FILE)

    fun tincd() = File(binDir(), TINCD_BIN)
    fun tinc() = File(binDir(), TINC_BIN)

}
