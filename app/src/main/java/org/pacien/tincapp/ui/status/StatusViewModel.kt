/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.repository.NodeInfo
import org.pacien.tincapp.repository.SubnetInfo
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState
import java.util.LinkedList

data class StatusUiState(
    val vpnState: VpnState = VpnState.Disconnected,
    val nodes: List<NodeInfo> = emptyList(),
    val subnets: List<SubnetInfo> = emptyList(),
    val graphData: String = "",
    val logs: List<String> = emptyList(),
    val selectedNodeInfo: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatusViewModel : ViewModel() {

    private val _selectedNodeInfo = MutableStateFlow<String?>(null)

    private val logLevelNumeric = 5
    private val logLineSize = 250

    val logs: StateFlow<List<String>> = VpnRepository.vpnState
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

    val uiState: StateFlow<StatusUiState> = combine(
        VpnRepository.vpnState,
        VpnRepository.nodes,
        VpnRepository.subnets,
        VpnRepository.graph,
        logs,
        _selectedNodeInfo
    ) { params: Array<Any?> ->
        StatusUiState(
            vpnState = params[0] as VpnState,
            nodes = params[1] as List<NodeInfo>,
            subnets = params[2] as List<SubnetInfo>,
            graphData = params[3] as String,
            logs = params[4] as List<String>,
            selectedNodeInfo = params[5] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatusUiState()
    )

    fun onNodeClick(nodeName: String) {
        val netName = (VpnRepository.vpnState.value as? VpnState.Connected)?.netName ?: return
        viewModelScope.launch {
            try {
                _selectedNodeInfo.value = Tinc.info(netName, nodeName)
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun dismissNodeInfo() {
        _selectedNodeInfo.value = null
    }
}
