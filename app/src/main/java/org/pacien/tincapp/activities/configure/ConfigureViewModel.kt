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

package org.pacien.tincapp.activities.configure

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.utils.makePublic
import java.util.regex.Pattern
import java.util.concurrent.CompletableFuture
import org.pacien.tincapp.extensions.Java.failedFuture

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

    fun resetState() {
        _uiState.value = ConfigureUiState.Idle
    }

    private fun validateNetName(netName: String): CompletableFuture<Unit> =
        if (networkNamePattern.matcher(netName).matches())
            CompletableFuture.completedFuture(Unit)
        else
            failedFuture(NetworkNameInvalidException())

    private fun execAction(labelResId: Int, action: CompletableFuture<Unit>, successResId: Int = R.string.configure_tools_message_network_configuration_written) {
        _uiState.value = ConfigureUiState.Loading(labelResId)
        action.whenComplete { _, throwable ->
            if (throwable != null) {
                val cause = throwable.cause ?: throwable
                if (cause is NetworkNameInvalidException) {
                    _uiState.value = ConfigureUiState.Error(messageResId = R.string.configure_tools_message_invalid_network_name)
                } else {
                    _uiState.value = ConfigureUiState.Error(message = cause.localizedMessage ?: "Unknown error")
                }
            } else {
                _uiState.value = ConfigureUiState.Success(successResId)
            }
        }
    }

    fun generateConf(netName: String, nodeName: String) {
        execAction(
            R.string.configure_tools_generate_config_generating,
            validateNetName(netName)
                .thenCompose { Tinc.init(netName, nodeName) }
                .thenCompose { TincApp.removeScripts(netName) }
                .thenCompose { TincApp.generateIfaceCfgTemplate(netName) }
                .thenApply { AppPaths.confDir(netName).makePublic() }
                .thenApply { Unit }
        )
    }

    fun joinNetwork(netName: String, url: String) {
        execAction(
            R.string.configure_tools_join_network_joining,
            validateNetName(netName)
                .thenCompose { Tinc.join(netName, url) }
                .thenCompose { TincApp.removeScripts(netName) }
                .thenCompose { TincApp.generateIfaceCfg(netName) }
                .thenApply { AppPaths.confDir(netName).makePublic() }
                .thenApply { Unit }
        )
    }

    fun lockNetwork(netName: String, passphrase: String) {
        execAction(
            labelResId = R.string.configure_tools_private_keys_encryption_encrypting,
            action = validateNetName(netName)
                .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) }
                .thenApply { Unit }
        )
    }

    fun unlockNetwork(netName: String, passphrase: String) {
        execAction(
            labelResId = R.string.configure_tools_private_keys_encryption_encrypting,
            action = validateNetName(netName)
                .thenCompose { TincApp.setPassphrase(netName, currentPassphrase = passphrase, newPassphrase = "") }
                .thenApply { Unit }
        )
    }

    fun deleteNetwork(netName: String) {
        execAction(
            labelResId = R.string.configure_tools_delete_network_deleting,
            successResId = R.string.configure_tools_message_network_deleted,
            action = validateNetName(netName)
                .thenCompose { TincApp.deleteNetwork(netName) }
                .thenApply { Unit }
        )
    }

    private class NetworkNameInvalidException : Exception()
}
