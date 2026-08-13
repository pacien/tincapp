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

package org.pacien.tincapp.ui.netlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.keyring.TincKeyring

/**
 * ViewModel for StartActivity.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StartViewModel(application: Application) : AndroidViewModel(application) {
    private val appPaths = AppPaths
    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val networkList: StateFlow<List<NetworkInfo>> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                while (true) {
                    val networkList = appPaths.confDir().list()?.sorted()?.map {
                        NetworkInfo(it, TincKeyring.needsPassphrase(it))
                    } ?: emptyList()
                    emit(networkList)
                    delay(1000)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }
}
