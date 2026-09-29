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

package org.pacien.tincapp.ui.netlist.configure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.vpnservice.TincVpnService
import org.pacien.tincapp.extensions.Files.makePublic
import java.util.regex.Pattern

sealed class ConfigureUiState {
    object Idle : ConfigureUiState()
    data class Loading(val messageResId: Int) : ConfigureUiState()
    data class Success(val messageResId: Int) : ConfigureUiState()
    data class Error(val message: String? = null, val messageResId: Int? = null) : ConfigureUiState()
}

class ConfigureViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ConfigureUiState>(ConfigureUiState.Idle)
    val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()

    private val networkNamePattern = Pattern.compile("^[^\\x00/]*$")
    private val reservedNetworkNames = setOf("", ".", "..")

    fun resetState() {
        _uiState.value = ConfigureUiState.Idle
    }

    private fun validateNetName(netName: String) {
        if (netName in reservedNetworkNames || !networkNamePattern.matcher(netName).matches()) {
            throw NetworkNameInvalidException()
        }
    }

    private fun execAction(labelResId: Int, successResId: Int = R.string.configure_tools_message_network_configuration_written, action: suspend () -> Unit) {
        _uiState.value = ConfigureUiState.Loading(labelResId)
        viewModelScope.launch {
            try {
                action()
                _uiState.value = ConfigureUiState.Success(successResId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = when {
                    e is NetworkNameInvalidException -> ConfigureUiState.Error(messageResId = R.string.configure_tools_message_invalid_network_name)
                    e is NetworkInUseException -> ConfigureUiState.Error(messageResId = R.string.configure_tools_message_network_in_use)
                    e.localizedMessage != null -> ConfigureUiState.Error(message = e.localizedMessage)
                    else -> ConfigureUiState.Error(messageResId = R.string.generic_error_unknown)
                }
            }
        }
    }

    fun generateConf(netName: String, nodeName: String) {
        execAction(R.string.configure_tools_generate_config_generating) {
            validateNetName(netName)
            Tinc.init(netName, nodeName)
            TincApp.removeScripts(netName)
            TincApp.generateIfaceCfgTemplate(netName)
            AppPaths.confDir(netName).makePublic()
        }
    }

    fun joinNetwork(netName: String, url: String) {
        execAction(R.string.configure_tools_join_network_joining) {
            validateNetName(netName)
            Tinc.join(netName, url)
            TincApp.removeScripts(netName)
            TincApp.generateIfaceCfg(netName)
            AppPaths.confDir(netName).makePublic()
        }
    }

    fun lockNetwork(netName: String, passphrase: String) {
        execAction(R.string.configure_tools_lock_network_progress) {
            validateNetName(netName)
            TincApp.setPassphrase(netName, newPassphrase = passphrase)
        }
    }

    fun unlockNetwork(netName: String, passphrase: String) {
        execAction(R.string.configure_tools_unlock_network_progress) {
            validateNetName(netName)
            TincApp.setPassphrase(netName, currentPassphrase = passphrase, newPassphrase = "")
        }
    }

    fun deleteNetwork(netName: String) {
        execAction(
            labelResId = R.string.configure_tools_delete_network_deleting,
            successResId = R.string.configure_tools_message_network_deleted
        ) {
            validateNetName(netName)
            ensureNotConnected(netName)
            TincApp.deleteNetwork(netName)
        }
    }

    private fun ensureNotConnected(netName: String) {
        if (netName == TincVpnService.getCurrentNetName()) {
            throw NetworkInUseException()
        }
    }

    private class NetworkNameInvalidException : Exception()
    private class NetworkInUseException : Exception()
}
