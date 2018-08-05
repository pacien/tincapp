/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.context

import android.os.Build
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R

/**
 * @author pacien
 */
object AppInfo {
  private fun appVersion(): String = App.getResources().getString(
    R.string.info_version_format,
    BuildConfig.VERSION_NAME,
    BuildConfig.BUILD_TYPE)

  private fun androidVersion(): String = App.getResources().getString(
    R.string.info_running_on_format,
    Build.VERSION.CODENAME,
    Build.VERSION.RELEASE)

  private fun supportedABIs(): String = App.getResources().getString(
    R.string.info_supported_abis_format,
    Build.SUPPORTED_ABIS.joinToString(","))

  fun all(): String = listOf(
    appVersion(),
    androidVersion(),
    supportedABIs()).joinToString("\n")
}
