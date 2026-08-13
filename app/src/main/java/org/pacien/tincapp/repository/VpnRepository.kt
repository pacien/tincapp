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

package org.pacien.tincapp.repository

import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.pacien.tincapp.config.VpnInterfaceConfiguration
import org.pacien.tincapp.context.App

/**
 * Represents the current state of the VPN service.
 */
sealed class VpnState {
    object Disconnected : VpnState()
    object Connecting : VpnState()
    data class Connected(val netName: String) : VpnState()
}

/**
 * Singleton repository managing the VPN state and persistence.
 */
object VpnRepository {

    private val dataStore = VpnDataStore(App.getContext())
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow<VpnState>(VpnState.Disconnected)
    val vpnState: StateFlow<VpnState> = _state.asStateFlow()

    private var _interfaceConfig: VpnInterfaceConfiguration? = null
    val interfaceConfig: VpnInterfaceConfiguration?
        get() = _interfaceConfig

    var fd: ParcelFileDescriptor? = null
    var daemon: Job? = null

    var currentNetName: String? = null
        private set
    var currentPassphrase: String? = null
        private set

    init {
        scope.launch {
            dataStore.netName.collect { currentNetName = it }
        }
        scope.launch {
            dataStore.passphrase.collect { currentPassphrase = it }
        }
    }

    val isConnected: Boolean
        get() = _state.value is VpnState.Connected || _state.value is VpnState.Connecting

    fun updateState(newState: VpnState) {
        _state.value = newState
    }

    fun setState(
        netName: String?,
        passphrase: String?,
        interfaceCfg: VpnInterfaceConfiguration?,
        fd: ParcelFileDescriptor?,
        daemon: Job?
    ) {
        scope.launch {
            dataStore.saveConnection(netName, passphrase)
        }
        currentNetName = netName
        currentPassphrase = passphrase
        _interfaceConfig = interfaceCfg
        this.fd = fd
        this.daemon = daemon
    }

    suspend fun getNetName(): String? = currentNetName ?: dataStore.netName.first()
    suspend fun getPassphrase(): String? = currentPassphrase ?: dataStore.passphrase.first()
}
