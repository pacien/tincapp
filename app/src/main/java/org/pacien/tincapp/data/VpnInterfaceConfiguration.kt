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

package org.pacien.tincapp.data

import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.pacien.tincapp.extensions.ApacheConfiguration.getCidrList
import org.pacien.tincapp.extensions.ApacheConfiguration.getIntList
import org.pacien.tincapp.extensions.ApacheConfiguration.getStringList
import org.pacien.tincapp.extensions.Java.applyIgnoringException
import java.io.File

/**
 * @author pacien
 */
data class VpnInterfaceConfiguration(val addresses: List<CidrAddress> = emptyList(),
                                     val routes: List<CidrAddress> = emptyList(),
                                     val dnsServers: List<String> = emptyList(),
                                     val searchDomains: List<String> = emptyList(),
                                     val allowedApplications: List<String> = emptyList(),
                                     val disallowedApplications: List<String> = emptyList(),
                                     val allowedFamilies: List<Int> = emptyList(),
                                     val allowBypass: Boolean = false,
                                     val blocking: Boolean = false,
                                     val mtu: Int? = null) {
  companion object {
    private const val KEY_ADDRESSES = "Address"
    private const val KEY_ROUTES = "Route"
    private const val KEY_DNS_SERVERS = "DNSServer"
    private const val KEY_SEARCH_DOMAINS = "SearchDomain"
    private const val KEY_ALLOWED_APPLICATIONS = "AllowApplication"
    private const val KEY_DISALLOWED_APPLICATIONS = "DisallowApplication"
    private const val KEY_ALLOWED_FAMILIES = "AllowFamily"
    private const val KEY_ALLOW_BYPASS = "AllowBypass"
    private const val KEY_BLOCKING = "Blocking"
    private const val KEY_MTU = "MTU"

    private const val INVITATION_KEY_ADDRESSES = "Ifconfig"
    private const val INVITATION_KEY_ROUTES = "Route"

    fun fromIfaceConfiguration(f: File) = fromIfaceConfiguration(Configurations().properties(f))
    private fun fromIfaceConfiguration(c: Configuration) = VpnInterfaceConfiguration(
      c.getCidrList(KEY_ADDRESSES),
      c.getCidrList(KEY_ROUTES),
      c.getStringList(KEY_DNS_SERVERS),
      c.getStringList(KEY_SEARCH_DOMAINS),
      c.getStringList(KEY_ALLOWED_APPLICATIONS),
      c.getStringList(KEY_DISALLOWED_APPLICATIONS),
      c.getIntList(KEY_ALLOWED_FAMILIES),
      c.getBoolean(KEY_ALLOW_BYPASS, false),
      c.getBoolean(KEY_BLOCKING, false),
      c.getInteger(KEY_MTU, null))

    fun fromInvitation(f: File) = fromInvitation(Configurations().properties(f))
    private fun fromInvitation(c: Configuration) = VpnInterfaceConfiguration(
      c.getStringList(INVITATION_KEY_ADDRESSES)
        .map { applyIgnoringException(CidrAddress.Companion::fromSlashSeparated, it) }
        .filterNotNull(),
      c.getStringList(INVITATION_KEY_ROUTES)
        .map { it.substringBefore(' ') }
        .map { CidrAddress.fromSlashSeparated(it) })
  }

  fun write(f: File) = FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration::class.java)
    .configure(Parameters().properties().setFile(f.apply { createNewFile() })).let { builder ->
      builder.configuration.let { cfg ->
        addresses.forEach { cfg.addProperty(KEY_ADDRESSES, it.toSlashSeparated()) }
        routes.forEach { cfg.addProperty(KEY_ROUTES, it.toSlashSeparated()) }
      }
      builder.save()
    }
}
