package org.pacien.tincapp.extensions

import android.net.VpnService
import org.pacien.tincapp.data.CidrAddress
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException

/**
 * @author pacien
 */
object VpnServiceBuilder {

  fun VpnService.Builder.addAddress(cidr: CidrAddress): VpnService.Builder = addAddress(cidr.address, cidr.prefix)
  fun VpnService.Builder.addRoute(cidr: CidrAddress): VpnService.Builder = addRoute(cidr.address, cidr.prefix)
  fun VpnService.Builder.allowBypass(allow: Boolean): VpnService.Builder = if (allow) allowBypass() else this
  fun VpnService.Builder.overrideMtu(mtu: Int?): VpnService.Builder = if (mtu != null) setMtu(mtu) else this

  fun VpnService.Builder.addAddresses(cidrList: List<CidrAddress>): VpnService.Builder =
    cidrList.fold(this, { net, cidr -> net.addAddress(cidr) })

  fun VpnService.Builder.addRoutes(cidrList: List<CidrAddress>): VpnService.Builder =
    cidrList.fold(this, { net, cidr -> net.addRoute(cidr) })

  fun VpnService.Builder.addDnsServers(dnsList: List<String>): VpnService.Builder =
    dnsList.fold(this, { net, dns -> net.addDnsServer(dns) })

  fun VpnService.Builder.addSearchDomains(domainList: List<String>): VpnService.Builder =
    domainList.fold(this, { net, domain -> net.addSearchDomain(domain) })

  fun VpnService.Builder.allowFamilies(familyList: List<Int>): VpnService.Builder =
    familyList.fold(this, { net, family -> net.allowFamily(family) })

  fun VpnService.Builder.addAllowedApplications(apps: List<String>): VpnService.Builder =
    apps.fold(this, { net, app -> applyIgnoringException(net::addAllowedApplication, app, net)!! })

  fun VpnService.Builder.addDisallowedApplications(apps: List<String>): VpnService.Builder =
    apps.fold(this, { net, app -> applyIgnoringException(net::addDisallowedApplication, app, net)!! })

  fun VpnService.Builder.applyCfg(cfg: VpnInterfaceConfiguration): VpnService.Builder = this
    .addAddresses(cfg.addresses)
    .addRoutes(cfg.routes)
    .addDnsServers(cfg.dnsServers)
    .addSearchDomains(cfg.searchDomains)
    .addAllowedApplications(cfg.allowedApplications)
    .addDisallowedApplications(cfg.disallowedApplications)
    .allowFamilies(cfg.allowedFamilies)
    .allowBypass(cfg.allowBypass)
    .setBlocking(cfg.blocking)
    .overrideMtu(cfg.mtu)

}
