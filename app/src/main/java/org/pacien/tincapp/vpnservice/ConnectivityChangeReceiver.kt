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

package org.pacien.tincapp.vpnservice

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.pacien.tincapp.commands.Tinc
import org.slf4j.LoggerFactory

/**
 * @author euxane
 */
object ConnectivityChangeReceiver {
  private val log by lazy { LoggerFactory.getLogger(this.javaClass)!! }
  private val tincCtl = Tinc
  private val tincVpnService = TincVpnService
  private var watchJob: Job? = null

  private fun watch(context: Context): Flow<Unit> = callbackFlow {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val callback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
        log.info("Network available: {}", network)
        trySend(Unit)
      }
    }
    connectivityManager.registerDefaultNetworkCallback(callback)
    awaitClose {
      try {
        connectivityManager.unregisterNetworkCallback(callback)
      } catch (e: Exception) {
        // already unregistered
      }
    }
  }

  private fun attemptReconnect(scope: CoroutineScope) {
    tincVpnService.getCurrentNetName()?.let { netName ->
      log.info("Sending immediate reconnection request to the tinc daemon.")
      scope.launch {
        try {
          tincCtl.retry(netName)
        } catch (e: Exception) {
          log.error("Failed to retry connection", e)
        }
      }
    }
  }

  fun registerWatcher(context: Context, scope: CoroutineScope) {
    watchJob?.cancel()
    watchJob = scope.launch {
      watch(context).collect {
        attemptReconnect(scope)
      }
    }
  }

  fun unregisterWatcher() {
    watchJob?.cancel()
    watchJob = null
  }
}
