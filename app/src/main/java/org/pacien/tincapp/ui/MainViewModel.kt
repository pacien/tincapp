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

package org.pacien.tincapp.ui

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.pacien.tincapp.context.App
import org.pacien.tincapp.repository.VpnEvent
import org.pacien.tincapp.repository.VpnRepository
import org.pacien.tincapp.repository.VpnState

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val vpnState: StateFlow<VpnState> = VpnRepository.vpnState

    var activeDialog by mutableStateOf<ActiveDialog?>(null)
        private set

    private val notificationManager = App.notificationManager
    private val notificationListener = OnSharedPreferenceChangeListener { _, _ ->
        checkPersistentError()
    }

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

        viewModelScope.launch {
            VpnRepository.events.collect { event ->
                when (event) {
                    is VpnEvent.Error -> showError(
                        msg = event.msg,
                        title = App.getContext().getString(event.titleRes),
                        docTopic = event.docTopic,
                        configDir = event.configDir,
                        proposeLogs = event.proposeLogs
                    )
                    is VpnEvent.ShowPassphraseDialog -> showDecryptKeys()
                }
            }
        }

        notificationManager.registerListener(notificationListener)
        checkPersistentError()
    }

    private fun checkPersistentError() {
        notificationManager.getError()?.let { error ->
            showError(
                msg = error.message,
                title = error.title,
                docTopic = error.manualLink,
                configDir = error.configDir,
                proposeLogs = error.proposeLogs
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        notificationManager.unregisterListener(notificationListener)
    }

    sealed class ActiveDialog {
        data class Progress(val text: String) : ActiveDialog()
        object DecryptKeys : ActiveDialog()
        object About : ActiveDialog()
        data class Error(
            val title: String? = null,
            val msg: String,
            val docTopic: String? = null,
            val configDir: String? = null,
            val proposeLogs: Boolean = false
        ) : ActiveDialog()
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

    fun showError(
        msg: String,
        title: String? = null,
        docTopic: String? = null,
        configDir: String? = null,
        proposeLogs: Boolean = false
    ) {
        activeDialog = ActiveDialog.Error(title, msg, docTopic, configDir, proposeLogs)
    }

    fun showWarning(msg: String, docTopic: String? = null, onContinue: () -> Unit) {
        activeDialog = ActiveDialog.Warning(msg, docTopic, onContinue)
    }

    fun showCrash(message: String, onSendReport: () -> Unit) {
        activeDialog = ActiveDialog.Crash(message, onSendReport)
    }

    fun dismissDialog() {
        if (activeDialog is ActiveDialog.Error) {
            notificationManager.dismissAll()
        }
        activeDialog = null
    }
}
