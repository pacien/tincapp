/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
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

import android.arch.lifecycle.LiveData
import org.pacien.tincapp.context.AppPaths
import java.util.*
import kotlin.concurrent.timer

/**
 * @author pacien
 */
class NetworkListLiveData : LiveData<List<String>>() {
  private val updateInterval = 2 * 1000L // in milliseconds
  private val appPaths = AppPaths
  private lateinit var updateTimer: Timer

  override fun onActive() {
    updateTimer = timer(period = updateInterval, action = { updateNetworkList() })
  }

  override fun onInactive() {
    updateTimer.apply { cancel() }.apply { purge() }
  }

  private fun updateNetworkList() {
    val networkList = appPaths.confDir().list()?.sorted() ?: emptyList()
    postValue(networkList)
  }
}
