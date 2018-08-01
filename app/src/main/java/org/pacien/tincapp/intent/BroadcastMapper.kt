/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.intent

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import org.pacien.tincapp.context.App

/**
 * @author pacien
 */
class BroadcastMapper(private val actionHandlers: Map<String, () -> Unit>) : BroadcastReceiver() {
  private val broadcastManager = LocalBroadcastManager.getInstance(App.getContext())
  private val intentFilter = actionHandlers.keys.fold(IntentFilter(), { filter, action -> filter.apply { addAction(action) } })

  fun register() = broadcastManager.registerReceiver(this, intentFilter)
  fun unregister() = broadcastManager.unregisterReceiver(this)
  override fun onReceive(context: Context?, intent: Intent?) = actionHandlers[intent?.action]?.invoke() ?: Unit
}
