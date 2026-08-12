/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2019 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.common

import androidx.lifecycle.LiveData
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * @author euxane
 */
abstract class SelfRefreshingLiveData<T>(private val refreshInterval: Long, private val timeUnit: TimeUnit) : LiveData<T>() {
  private val scheduledExecutor = Executors.newSingleThreadScheduledExecutor()
  private lateinit var scheduledFuture: ScheduledFuture<*>

  override fun onActive() {
    scheduledFuture = scheduledExecutor.scheduleWithFixedDelay(this::onRefresh, 0, refreshInterval, timeUnit)
  }

  override fun onInactive() {
    scheduledFuture.cancel(false)
  }

  abstract fun onRefresh()
}
