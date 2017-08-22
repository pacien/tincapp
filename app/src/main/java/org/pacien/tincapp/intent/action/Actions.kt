package org.pacien.tincapp.intent.action

import org.pacien.tincapp.BuildConfig

/**
 * @author pacien
 */

private val PREFIX = "${BuildConfig.APPLICATION_ID}.intent.action"

val ACTION_CONNECT = "$PREFIX.CONNECT"
val ACTION_DISCONNECT = "$PREFIX.DISCONNECT"

val TINC_SCHEME = "tinc"
