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

package org.pacien.tincapp.activities.start

import android.app.Application
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.pacien.tincapp.context.AppNotificationManager
import org.pacien.tincapp.context.AppNotificationManager.ErrorNotification

/**
 * ViewModel for StartActivity.
 */
class StartViewModel(application: Application) : AndroidViewModel(application) {
    private val notificationManager = AppNotificationManager(application)
    private val networkListLiveData = NetworkListLiveData()

    private val _errorNotification = MutableLiveData<ErrorNotification?>(notificationManager.getError())
    val errorNotification: LiveData<ErrorNotification?> = _errorNotification

    val networkList: LiveData<List<NetworkInfo>> = networkListLiveData

    private val notificationListener = OnSharedPreferenceChangeListener { _, _ ->
        _errorNotification.postValue(notificationManager.getError())
    }

    init {
        notificationManager.registerListener(notificationListener)
    }

    override fun onCleared() {
        notificationManager.unregisterListener(notificationListener)
    }

    fun refresh() {
        networkListLiveData.onRefresh()
        _errorNotification.value = notificationManager.getError()
    }
}
