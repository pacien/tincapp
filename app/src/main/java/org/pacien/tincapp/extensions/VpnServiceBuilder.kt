/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.extensions

import android.net.VpnService
import org.pacien.tincapp.data.CidrAddress
import org.pacien.tincapp.data.VpnInterfaceConfiguration
import org.pacien.tincapp.extensions.Java.applyIgnoringException

/**
 * @author pacien
 */
object VpnServiceBuilder {
  private fun <T> exceptWithCidr(cidr: CidrAddress, func: () -> T) = try {
    func()
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException("${e.message}: $cidr")
  }

  private fun VpnService.Builder.addAddress(cidr: CidrAddress): VpnService.Builder =
    exceptWithCidr(cidr, { addAddress(cidr.address, cidr.prefix) })

  private fun VpnService.Builder.addRoute(cidr: CidrAddress): VpnService.Builder =
    exceptWithCidr(cidr, { addRoute(cidr.address, cidr.prefix) })

  private fun VpnService.Builder.allowBypass(allow: Boolean): VpnService.Builder =
    if (allow) allowBypass() else this

  private fun VpnService.Builder.overrideMtu(mtu: Int?): VpnService.Builder =
    if (mtu != null) setMtu(mtu) else this

  private fun VpnService.Builder.addAddresses(cidrList: List<CidrAddress>): VpnService.Builder =
    cidrList.fold(this, { net, cidr -> net.addAddress(cidr) })

  private fun VpnService.Builder.addRoutes(cidrList: List<CidrAddress>): VpnService.Builder =
    cidrList.fold(this, { net, cidr -> net.addRoute(cidr) })

  private fun VpnService.Builder.addDnsServers(dnsList: List<String>): VpnService.Builder =
    dnsList.fold(this, { net, dns -> net.addDnsServer(dns) })

  private fun VpnService.Builder.addSearchDomains(domainList: List<String>): VpnService.Builder =
    domainList.fold(this, { net, domain -> net.addSearchDomain(domain) })

  private fun VpnService.Builder.allowFamilies(familyList: List<Int>): VpnService.Builder =
    familyList.fold(this, { net, family -> net.allowFamily(family) })

  private fun VpnService.Builder.addAllowedApplications(apps: List<String>): VpnService.Builder =
    apps.fold(this, { net, app -> applyIgnoringException(net::addAllowedApplication, app, net)!! })

  private fun VpnService.Builder.addDisallowedApplications(apps: List<String>): VpnService.Builder =
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
