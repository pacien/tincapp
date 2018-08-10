/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.activities.status

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.pacien.tincapp.databinding.StatusNetworkInfoFragmentBinding
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class NetworkInfoFragment : Fragment() {
  private val vpnService by lazy { TincVpnService }
  private val netName by lazy { vpnService.getCurrentNetName() }
  private val interfaceConfiguration by lazy { vpnService.getCurrentInterfaceCfg() }
  private val listFormatter = VpnInterfaceConfigurationFormatter

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val binding = StatusNetworkInfoFragmentBinding.inflate(inflater, container, false)
    binding.netName = netName
    binding.vpnInterfaceConfiguration = interfaceConfiguration
    binding.listFormatter = listFormatter
    return binding.root
  }
}
