/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2018 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.activities.status.subnets

import org.pacien.tincapp.activities.common.SelfRefreshingLiveData
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.service.TincVpnService
import java.util.concurrent.TimeUnit

/**
 * @author euxane
 */
class SubnetListLiveData : SelfRefreshingLiveData<List<SubnetInfo>>(5, TimeUnit.SECONDS) {
  private val vpnService = TincVpnService
  private val tincCtl = Tinc

  override fun onRefresh() {
    vpnService.getCurrentNetName()
      ?.let { netName -> tincCtl.dumpSubnets(netName) }
      ?.thenApply { list -> list.map { SubnetInfo.ofSubnetDump(it) } }
      ?.thenAccept(this::postValue)
  }
}
