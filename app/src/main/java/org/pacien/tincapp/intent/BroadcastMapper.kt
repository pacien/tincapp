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
