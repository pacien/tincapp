package org.pacien.tincapp.commands

import java8.util.concurrent.CompletableFuture
import org.pacien.tincapp.commands.Executor.runAsyncTask
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.data.VpnInterfaceConfiguration

/**
 * @author pacien
 */
object TincApp {

    private val SCRIPT_SUFFIXES = listOf("-up", "-down", "-created", "-accepted")
    private val STATIC_SCRIPTS = listOf("tinc", "host", "subnet", "invitation").flatMap { s -> SCRIPT_SUFFIXES.map { s + it } }

    private fun listScripts(netName: String) = AppPaths.confDir(netName).listFiles { f -> f.name in STATIC_SCRIPTS } +
            AppPaths.hostsDir(netName).listFiles { f -> SCRIPT_SUFFIXES.any { f.name.endsWith(it) } }

    fun removeScripts(netName: String): CompletableFuture<Void> = runAsyncTask {
        listScripts(netName).forEach { it.delete() }
    }

    fun generateIfaceCfg(netName: String): CompletableFuture<Void> = runAsyncTask {
        VpnInterfaceConfiguration
                .fromInvitation(AppPaths.invitationFile(netName))
                .write(AppPaths.netConfFile(netName))
    }

}
