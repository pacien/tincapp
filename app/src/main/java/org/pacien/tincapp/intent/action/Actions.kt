package org.pacien.tincapp.intent.action

import org.pacien.tincapp.BuildConfig

/**
 * @author pacien
 */

private val PREFIX = "${BuildConfig.APPLICATION_ID}.intent.action"

val ACTION_CONNECT = "$PREFIX.CONNECT"
val ACTION_DISCONNECT = "$PREFIX.DISCONNECT"

val ACTION_START_SERVICE = "$PREFIX.START_SERVICE"
val ACTION_STOP_SERVICE = "$PREFIX.STOP_SERVICE"

val TINC_SCHEME = "tinc"
