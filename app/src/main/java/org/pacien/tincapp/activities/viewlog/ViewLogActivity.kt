/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2019 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.activities.viewlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProviders
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.ui.theme.TincTheme

/**
 * @author euxane
 */
class ViewLogActivity : BaseActivity() {
  private val viewModel by lazy { ViewModelProviders.of(this)[LogViewModel::class.java] }
  private var isLoggingEnabled by mutableStateOf(true)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    isLoggingEnabled = viewModel.logging

    setContent {
      TincTheme {
        val logLines by viewModel.log.observeAsState(emptyList())
        val logText = logLines.joinToString("\n\n")

        ViewLogScreen(
          logText = logText,
          logLevelText = viewModel.logLevelText,
          isLoggingEnabled = isLoggingEnabled,
          onBackClick = { finish() },
          onToggleLogging = { toggleLogging() },
          onShareClick = { shareLog() }
        )
      }
    }
  }

  private fun shareLog() {
    val logSnippet = viewModel.log.value?.joinToString("\n")
    val shareIntent = Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, logSnippet)

    startActivityChooser(shareIntent, resources.getString(R.string.log_view_menu_share_log))
  }

  private fun toggleLogging() {
    isLoggingEnabled = !isLoggingEnabled
    viewModel.logging = isLoggingEnabled
  }
}
