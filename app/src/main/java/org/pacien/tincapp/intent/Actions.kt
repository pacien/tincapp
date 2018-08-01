/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2018 Pacien TRAN-GIRARD
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

import android.net.Uri
import org.pacien.tincapp.BuildConfig

/**
 * @author pacien
 */
object Actions {
  const val PREFIX = "${BuildConfig.APPLICATION_ID}.intent.action"
  const val ACTION_CONNECT = "$PREFIX.CONNECT"
  const val ACTION_DISCONNECT = "$PREFIX.DISCONNECT"
  const val EVENT_CONNECTED = "$PREFIX.CONNECTED"
  const val EVENT_DISCONNECTED = "$PREFIX.DISCONNECTED"
  const val EVENT_ABORTED = "$PREFIX.ABORTED"
  const val TINC_SCHEME = "tinc"

  fun buildNetworkUri(netName: String, passphrase: String? = null): Uri =
    Uri.Builder().scheme(Actions.TINC_SCHEME).opaquePart(netName).fragment(passphrase).build()
}
