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

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.context.App
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState
import java.util.LinkedList
import java.util.regex.Matcher
import java.util.regex.Pattern

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

    val nodes: StateFlow<List<NodeInfo>> = VpnRepository.vpnState
        .flatMapLatest { state ->
            if (state is VpnState.Connected) {
                flow {
                    while (true) {
                        try {
                            val dump = Tinc.dumpNodes(state.netName)
                            emit(dump.map { NodeInfo.ofNodeDump(it) })
                        } catch (_: Exception) {
                        }
                        delay(5000)
                    }
                }.flowOn(Dispatchers.IO)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val subnets: StateFlow<List<SubnetInfo>> = VpnRepository.vpnState
        .flatMapLatest { state ->
            if (state is VpnState.Connected) {
                flow {
                    while (true) {
                        try {
                            val dump = Tinc.dumpSubnets(state.netName)
                            emit(dump.map { SubnetInfo.ofSubnetDump(it) })
                        } catch (_: Exception) {
                        }
                        delay(5000)
                    }
                }.flowOn(Dispatchers.IO)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val graphData: StateFlow<String> = VpnRepository.vpnState
        .flatMapLatest { state ->
            if (state is VpnState.Connected) {
                flow {
                    while (true) {
                        try {
                            emit(Tinc.dumpGraph(state.netName))
                        } catch (_: Exception) {
                        }
                        delay(5000)
                    }
                }.flowOn(Dispatchers.IO)
            } else {
                flowOf("")
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val uiState: StateFlow<StatusUiState> = combine(
        VpnRepository.vpnState,
        nodes,
        subnets,
        graphData,
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

/**
 * @author euxane
 */
data class SubnetInfo(val ipRange: String, val owner: String) {
  companion object {
    private const val SUBNET_DUMP_PATTERN_STRING = "(\\S+) owner (\\S+)"
    private val SUBNET_DUMP_PATTERN by lazy { Pattern.compile(SUBNET_DUMP_PATTERN_STRING) }

    fun ofSubnetDump(line: String) = ofSubnetDump(SUBNET_DUMP_PATTERN.matcher(line).apply { find() })
    private fun ofSubnetDump(matcher: Matcher) = SubnetInfo(ipRange = matcher[1]!!, owner = matcher[2]!!)
    private operator fun Matcher.get(index: Int) = group(index)
  }
}

/**
 * @author euxane
 */
data class NodeInfo(val name: String,
                    val id: String,
                    val ip: String,
                    val port: String,
                    val cipher: Int,
                    val digest: Int,
                    val macLength: Int,
                    val compression: Int,
                    val options: Int,
                    val status: Int,
                    val nextHop: String,
                    val via: String,
                    val distance: Int,
                    val pMtu: Int,
                    val minMtu: Int,
                    val maxMtu: Int) {

  // https://github.com/gsliepen/tinc/blob/950bbc8f2f9c580ac85bef7bab9a3ae36ea99c4b/src/info.c#L174
  fun reachabilityText(resources: Resources = App.getResources()): String = when {
    ip == "MYSELF" -> resources.getString(R.string.status_node_reachability_this_node)
    distance == -1 -> resources.getString(R.string.status_node_reachability_unreachable)
    minMtu > 0 || nextHop == name -> resources.getString(R.string.status_node_reachability_direct_connection)
    distance > 1 -> resources.getString(R.string.status_node_reachability_via_format, nextHop)
    else -> resources.getString(R.string.status_node_reachability_unknown)
  }

  companion object {
    private const val NODE_DUMP_PATTERN_STRING =
      "(\\S+) " +
        "id (\\S+) " +
        "at (\\S+) " +
        "port (\\S+) " +
        "cipher (\\S+) " +
        "digest (\\S+) " +
        "maclength (\\S+) " +
        "compression (\\S+) " +
        "options (\\S+) " +
        "status (\\S+) " +
        "nexthop (\\S+) " +
        "via (\\S+) " +
        "distance (\\S+) " +
        "pmtu (\\S+) \\(min (\\S+) max (\\S+)\\)"

    private val NODE_DUMP_PATTERN by lazy { Pattern.compile(NODE_DUMP_PATTERN_STRING) }

    fun ofNodeDump(line: String) =
      ofNodeDump(NODE_DUMP_PATTERN.matcher(line).apply { find() })

    private fun ofNodeDump(matcher: Matcher) = NodeInfo(
      name = matcher[1]!!,
      id = matcher[2]!!,
      ip = matcher[3]!!,
      port = matcher[4]!!,
      cipher = matcher[5]!!.toInt(),
      digest = matcher[6]!!.toInt(),
      macLength = matcher[7]!!.toInt(),
      compression = matcher[8]!!.toInt(),
      options = matcher[9]!!.toInt(16),
      status = matcher[10]!!.toInt(16),
      nextHop = matcher[11]!!,
      via = matcher[12]!!,
      distance = matcher[13]!!.toInt(),
      pMtu = matcher[14]!!.toInt(),
      minMtu = matcher[15]!!.toInt(),
      maxMtu = matcher[16]!!.toInt()
    )

    private operator fun Matcher.get(index: Int) = group(index)
  }
}