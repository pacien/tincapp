package org.pacien.tincapp.service

/**
 * @author pacien
 */

import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.builder.fluent.Configurations
import java.io.File

private val KEY_ADDRESSES = "Address"
private val KEY_ROUTES = "Route"
private val KEY_DNS_SERVERS = "DNSServer"
private val KEY_SEARCH_DOMAINS = "SearchDomain"
private val KEY_ALLOWED_APPLICATIONS = "AllowApplication"
private val KEY_DISALLOWED_APPLICATIONS = "DisallowApplication"
private val KEY_ALLOWED_FAMILIES = "AllowFamily"
private val KEY_ALLOW_BYPASS = "AllowBypass"
private val KEY_BLOCKING = "Blocking"
private val KEY_MTU = "MTU"

private fun Configuration.getStringList(key: String): List<String> = getList(String::class.java, key, emptyList())
private fun Configuration.getCidrList(key: String): List<CidrAddress> = getStringList(key).map { CidrAddress(it) }
private fun Configuration.getIntList(key: String): List<Int> = getList(Int::class.java, key, emptyList())

data class CidrAddress(val address: String, val prefix: Int) {
    constructor(slashSeparated: String) :
            this(slashSeparated.substringBefore("/"), Integer.parseInt(slashSeparated.substringAfter("/")))
}

data class VpnInterfaceConfiguration(val addresses: List<CidrAddress>,
                                     val routes: List<CidrAddress>,
                                     val dnsServers: List<String>,
                                     val searchDomains: List<String>,
                                     val allowedApplications: List<String>,
                                     val disallowedApplications: List<String>,
                                     val allowedFamilies: List<Int>,
                                     val allowBypass: Boolean,
                                     val blocking: Boolean,
                                     val mtu: Int?) {

    constructor(cfg: Configuration) : this(
            cfg.getCidrList(KEY_ADDRESSES),
            cfg.getCidrList(KEY_ROUTES),
            cfg.getStringList(KEY_DNS_SERVERS),
            cfg.getStringList(KEY_SEARCH_DOMAINS),
            cfg.getStringList(KEY_ALLOWED_APPLICATIONS),
            cfg.getStringList(KEY_DISALLOWED_APPLICATIONS),
            cfg.getIntList(KEY_ALLOWED_FAMILIES),
            cfg.getBoolean(KEY_ALLOW_BYPASS, false),
            cfg.getBoolean(KEY_BLOCKING, false),
            cfg.getInteger(KEY_MTU, null))

    constructor(cfgFile: File) : this(Configurations().properties(cfgFile))

}
