/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2019 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.status.logviewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState
import java.util.LinkedList

/**
 * @author euxane
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LogViewModel : ViewModel() {
  private val logLevelNumeric = 5
  private val logLineSize = 250

  val log: StateFlow<List<String>> = VpnRepository.vpnState
    .flatMapLatest { state ->
      if (state is VpnState.Connected) {
        channelFlow {
          val logList = LinkedList<String>()
          val loggerProcess = Tinc.log(state.netName, logLevelNumeric)

          val captureJob = launch(Dispatchers.IO) {
            try {
              loggerProcess.inputStream?.use { inputStream ->
                inputStream.bufferedReader().useLines { lines ->
                  lines.forEach { line ->
                    synchronized(logList) {
                      if (logList.size >= logLineSize) logList.removeFirst()
                      logList.addLast(line)
                    }
                  }
                }
              }
            } catch (_: Exception) {
              // IOException is expected when the process is destroyed and streams are closed
            }
          }

          try {
            while (true) {
              val currentLog = synchronized(logList) { ArrayList(logList) }
              send(currentLog)
              delay(250)
            }
          } finally {
            loggerProcess.destroy()
            captureJob.cancel()
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
