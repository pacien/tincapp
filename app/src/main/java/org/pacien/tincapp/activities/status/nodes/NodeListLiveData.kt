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

package org.pacien.tincapp.activities.status.nodes

import org.pacien.tincapp.activities.common.SelfRefreshingLiveData
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.service.TincVpnService
import java.util.concurrent.TimeUnit

/**
 * @author pacien
 */
class NodeListLiveData : SelfRefreshingLiveData<List<NodeInfo>>(1, TimeUnit.SECONDS) {
  private val vpnService = TincVpnService
  private val tincCtl = Tinc

  override fun onRefresh() {
    vpnService.getCurrentNetName()
      ?.let { netName -> tincCtl.dumpNodes(netName) }
      ?.thenApply { list -> list.map { NodeInfo.ofNodeDump(it) } }
      ?.thenAccept(this::postValue)
  }
}
