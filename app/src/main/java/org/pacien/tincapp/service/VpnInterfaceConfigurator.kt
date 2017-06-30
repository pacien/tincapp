package org.pacien.tincapp.service

import android.net.VpnService
import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import java.io.File

/**
 * @author pacien
 */
object VpnInterfaceConfigurator {

    val KEY_ADDRESSES = "Address"
    val KEY_ROUTES = "Route"
    val KEY_DNS_SERVERS = "DNSServer"
    val KEY_SEARCH_DOMAINS = "SearchDomain"
    val KEY_ALLOWED_APPLICATIONS = "AllowApplication"
    val KEY_DISALLOWED_APPLICATIONS = "DisallowApplication"
    val KEY_ALLOWED_FAMILIES = "AllowFamily"
    val KEY_ALLOW_BYPASS = "AllowBypass"
    val KEY_BLOCKING = "Blocking"
    val KEY_MTU = "MTU"

    private fun Configuration.getStringList(key: String): List<String> =
            getList(String::class.java, key, emptyList())

    private fun Configuration.getCidrList(key: String): List<CidrAddress> =
            getStringList(key).map { CidrAddress(it) }

    private fun Configuration.getIntList(key: String): List<Int> =
            getList(Int::class.java, key, emptyList())

    fun applyConfiguration(net: VpnService.Builder, cfg: Configuration): VpnService.Builder = net
            .addAddresses(cfg.getCidrList(KEY_ADDRESSES))
            .addRoutes(cfg.getCidrList(KEY_ROUTES))
            .addDnsServers(cfg.getStringList(KEY_DNS_SERVERS))
            .addSearchDomains(cfg.getStringList(KEY_SEARCH_DOMAINS))
            .addAllowedApplications(cfg.getStringList(KEY_ALLOWED_APPLICATIONS))
            .addDisallowedApplications(cfg.getStringList(KEY_DISALLOWED_APPLICATIONS))
            .allowFamilies(cfg.getIntList(KEY_ALLOWED_FAMILIES))
            .allowBypass(cfg.getBoolean(KEY_ALLOW_BYPASS, false))
            .setBlocking(cfg.getBoolean(KEY_BLOCKING, false))
            .overrideMtu(cfg.getInteger(KEY_MTU, null))

    fun applyConfiguration(net: VpnService.Builder, cfg: File): VpnService.Builder = try {
        applyConfiguration(net, Configurations().properties(cfg))
    } catch (e: ConfigurationException) {
        throw IllegalArgumentException(e.message)
    }

}
