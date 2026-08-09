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

package org.pacien.tincapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import org.pacien.tincapp.commands.Tinc
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

/**
 * @author euxane
 */
object ConnectivityChangeReceiver : BroadcastReceiver() {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val tincCtl = Tinc
  private val tincVpnService = TincVpnService

  private val networkCallback = object : ConnectivityManager.NetworkCallback() {
    override fun onAvailable(network: Network) {
      log.info("Network available: {}", network)
      attemptReconnect()
    }
  }

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
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      connectivityManager.registerDefaultNetworkCallback(networkCallback)
    } else {
      val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
      context.registerReceiver(this, filter)
    }
  }

  fun unregisterWatcher(context: Context) {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      try {
        connectivityManager.unregisterNetworkCallback(networkCallback)
      } catch (e: Exception) {
        // already unregistered
      }
    } else {
      try {
        context.unregisterReceiver(this)
      } catch (e: IllegalArgumentException) {
        // already unregistered
      }
    }
  }
}
