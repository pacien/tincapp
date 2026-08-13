/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2020 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.status.nodes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState

/**
 * @author euxane
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NodeListViewModel : ViewModel() {
  private val tincCtl = Tinc

  val nodeList: StateFlow<List<NodeInfo>> = VpnRepository.vpnState
    .flatMapLatest { state ->
      if (state is VpnState.Connected) {
        flow {
          while (true) {
            try {
              val list = tincCtl.dumpNodes(state.netName)
              emit(list.map { NodeInfo.ofNodeDump(it) })
            } catch (e: Exception) {
              // handle error
            }
            delay(5000)
          }
        }
      } else {
        flowOf(emptyList())
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )
}
