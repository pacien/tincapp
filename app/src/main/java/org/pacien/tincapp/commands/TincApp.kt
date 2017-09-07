package org.pacien.tincapp.commands

import java8.util.concurrent.CompletableFuture
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Executor.runAsyncTask
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.TincConfiguration
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.utils.PemUtils
import java.io.FileNotFoundException

/**
 * @author pacien
 */
object TincApp {

    private val SCRIPT_SUFFIXES = listOf("-up", "-down", "-created", "-accepted")
    private val STATIC_SCRIPTS = listOf("tinc", "host", "subnet", "invitation").flatMap { s -> SCRIPT_SUFFIXES.map { s + it } }

    private fun listScripts(netName: String) = AppPaths.confDir(netName).listFiles { f -> f.name in STATIC_SCRIPTS } +
            AppPaths.hostsDir(netName).listFiles { f -> SCRIPT_SUFFIXES.any { f.name.endsWith(it) } }

    fun listPrivateKeys(netName: String) = try {
        TincConfiguration.fromTincConfiguration(AppPaths.existing(AppPaths.tincConfFile(netName))).let {
            listOf(
                    it.privateKeyFile ?: AppPaths.defaultRsaPrivateKeyFile(netName),
                    it.ed25519PrivateKeyFile ?: AppPaths.defaultEd25519PrivateKeyFile(netName))
        }
    } catch (e: FileNotFoundException) {
        throw FileNotFoundException(App.getResources().getString(R.string.message_network_config_not_found_format, e.message!!))
    }

    fun removeScripts(netName: String): CompletableFuture<Void> = runAsyncTask {
        listScripts(netName).forEach { it.delete() }
    }

    fun generateIfaceCfg(netName: String): CompletableFuture<Void> = runAsyncTask {
        VpnInterfaceConfiguration
                .fromInvitation(AppPaths.invitationFile(netName))
                .write(AppPaths.netConfFile(netName))
    }

    fun setPassphrase(netName: String, currentPassphrase: String? = null, newPassphrase: String?): CompletableFuture<Void> = runAsyncTask {
        listPrivateKeys(netName)
                .filter { it.exists() }
                .map { Pair(PemUtils.read(it), it) }
                .map { Pair(PemUtils.decrypt(it.first, currentPassphrase), it.second) }
                .map { Pair(if (newPassphrase?.isNotEmpty() == true) PemUtils.encrypt(it.first, newPassphrase) else it.first, it.second) }
                .forEach { PemUtils.write(it.first, it.second.writer()) }
    }

}
