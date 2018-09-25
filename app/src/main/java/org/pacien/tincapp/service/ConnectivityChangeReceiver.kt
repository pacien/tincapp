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

package org.pacien.tincapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import org.pacien.tincapp.commands.Tinc
import org.slf4j.LoggerFactory

/**
 * @author pacien
 */
object ConnectivityChangeReceiver : BroadcastReceiver() {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val tincCtl = Tinc
  private val tincVpnService = TincVpnService

  override fun onReceive(context: Context, intent: Intent) {
    log.info("Connectivity change intent received: {}", intent.toString())
    if (isNetworkAvailable(intent)) attemptReconnect()
  }

  private fun attemptReconnect() {
    tincVpnService.getCurrentNetName()?.let { netName ->
      log.info("Sending immediate reconnection request to the tinc daemon.")
      tincCtl.retry(netName)
    }
  }

  private fun isNetworkAvailable(intent: Intent) =
    !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)

  fun registerWatcher(context: Context) {
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(this, filter)
  }

  fun unregisterWatcher(context: Context) {
    context.unregisterReceiver(this)
  }
}
