/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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
import java.lang.IllegalArgumentException

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

  // TODO: ConnectivityManager.CONNECTIVITY_ACTION was deprecated in API level 28.
  //   "Apps should use the more versatile
  //   requestNetwork(NetworkRequest, PendingIntent),
  //   registerNetworkCallback(NetworkRequest, PendingIntent) or
  //   registerDefaultNetworkCallback(ConnectivityManager.NetworkCallback)
  //   functions instead for faster and more detailed updates
  //   about the network changes they care about."
  //   See https://developer.android.com/reference/android/net/ConnectivityManager
  fun registerWatcher(context: Context) {
    val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    context.registerReceiver(this, filter)
  }

  fun unregisterWatcher(context: Context) {
    try {
      context.unregisterReceiver(this)
    } catch (e: IllegalArgumentException) {
      // already unregistered
    }
  }
}
