package org.pacien.tincapp.service

import android.net.VpnService

/**
 * @author pacien
 */

data class CidrAddress(val address: String, val prefix: Int) {
    constructor(slashSeparated: String) :
            this(slashSeparated.substringBefore("/"), Integer.parseInt(slashSeparated.substringAfter("/")))
}


fun <A, R> applyIgnoringException(f: (A) -> R, x: A, alt: R? = null) = try {
    f(x)
} catch (_: Exception) {
    alt
}

fun VpnService.Builder.addAddress(cidr: CidrAddress) = addAddress(cidr.address, cidr.prefix)
fun VpnService.Builder.addRoute(cidr: CidrAddress) = addRoute(cidr.address, cidr.prefix)
fun VpnService.Builder.allowBypass(allow: Boolean) = if (allow) allowBypass() else this
fun VpnService.Builder.overrideMtu(mtu: Int?) = if (mtu != null) setMtu(mtu) else this

fun VpnService.Builder.addAddresses(cidrList: List<CidrAddress>) =
        cidrList.fold(this, { net, cidr -> net.addAddress(cidr) })

fun VpnService.Builder.addRoutes(cidrList: List<CidrAddress>) =
        cidrList.fold(this, { net, cidr -> net.addRoute(cidr) })

fun VpnService.Builder.addDnsServers(dnsList: List<String>) =
        dnsList.fold(this, { net, dns -> net.addDnsServer(dns) })

fun VpnService.Builder.addSearchDomains(domainList: List<String>) =
        domainList.fold(this, { net, domain -> net.addSearchDomain(domain) })

fun VpnService.Builder.allowFamilies(familyList: List<Int>) =
        familyList.fold(this, { net, family -> net.allowFamily(family) })

fun VpnService.Builder.addAllowedApplications(apps: List<String>) =
        apps.fold(this, { net, app -> applyIgnoringException(net::addAllowedApplication, app, net)!! })

fun VpnService.Builder.addDisallowedApplications(apps: List<String>) =
        apps.fold(this, { net, app -> applyIgnoringException(net::addDisallowedApplication, app, net)!! })
