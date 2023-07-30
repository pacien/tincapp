/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2023 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.start

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppNotificationManager
import org.pacien.tincapp.databinding.StartErrorNotificationBinding

/**
 * @author pacien
 */
class ErrorNotificationFragment : BaseFragment() {
  private val notificationManager by lazy { AppNotificationManager(context!!) }
  private val notificationListener = OnSharedPreferenceChangeListener { _, _ -> updateView() }
  private lateinit var viewBinding: StartErrorNotificationBinding

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    viewBinding = StartErrorNotificationBinding.inflate(inflater, container, false)
    updateView()
    return viewBinding.root
  }

  override fun onResume() {
    super.onResume()
    notificationManager.registerListener(notificationListener)
  }

  override fun onPause() {
    super.onPause()
    notificationManager.unregisterListener(notificationListener)
  }

  private fun updateView() {
    val maybeError = notificationManager.getError()
    viewBinding.errorNotification = maybeError
    viewBinding.openManualAction = { App.openURL(maybeError?.manualLink!!) }
  }
}
