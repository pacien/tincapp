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
