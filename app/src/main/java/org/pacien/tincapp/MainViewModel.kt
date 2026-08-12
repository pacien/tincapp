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

package org.pacien.tincapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.pacien.tincapp.intent.VpnEvents
import org.pacien.tincapp.intent.VpnState

class MainViewModel : ViewModel() {

    val vpnState: StateFlow<VpnState> = VpnEvents.state

    var activeDialog by mutableStateOf<ActiveDialog?>(null)
        private set

    init {
        viewModelScope.launch {
            vpnState.collect { state ->
                if (state is VpnState.Connected || state is VpnState.Disconnected) {
                    if (activeDialog is ActiveDialog.Progress) {
                        dismissDialog()
                    }
                }
            }
        }
    }

    sealed class ActiveDialog {
        data class Progress(val text: String) : ActiveDialog()
        object DecryptKeys : ActiveDialog()
        object About : ActiveDialog()
        data class Error(val msg: String, val docTopic: String? = null) : ActiveDialog()
        data class Warning(
            val msg: String,
            val docTopic: String? = null,
            val onContinue: () -> Unit
        ) : ActiveDialog()
        data class Crash(val message: String, val onSendReport: () -> Unit) : ActiveDialog()
    }

    fun showProgress(text: String) {
        activeDialog = ActiveDialog.Progress(text)
    }

    fun showDecryptKeys() {
        activeDialog = ActiveDialog.DecryptKeys
    }

    fun showAbout() {
        activeDialog = ActiveDialog.About
    }

    fun showError(msg: String, docTopic: String? = null) {
        activeDialog = ActiveDialog.Error(msg, docTopic)
    }

    fun showWarning(msg: String, docTopic: String? = null, onContinue: () -> Unit) {
        activeDialog = ActiveDialog.Warning(msg, docTopic, onContinue)
    }

    fun showCrash(message: String, onSendReport: () -> Unit) {
        activeDialog = ActiveDialog.Crash(message, onSendReport)
    }

    fun dismissDialog() {
        activeDialog = null
    }
}
