/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2019 Pacien TRAN-GIRARD
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

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.view_log_activity.*
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity

/**
 * @author pacien
 */
class ViewLogActivity : BaseActivity() {
  private val viewModel by lazy { ViewModelProviders.of(this).get(LogViewModel::class.java) }
  private val logObserver: Observer<List<String>> = Observer { showLog(it) }
  private var toggleButton: MenuItem? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar.setDisplayHomeAsUpEnabled(true)
    setContentView(R.layout.view_log_activity)
    enableLogging(viewModel.logging)
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_viewlog, m)
    toggleButton = m.findItem(R.id.log_viewer_action_toggle)
    return super.onCreateOptionsMenu(m)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return true
  }

  @Suppress("UNUSED_PARAMETER")
  fun shareLog(m: MenuItem) {
    val logSnippet = viewModel.log.value?.joinToString("\n")
    val shareIntent = Intent(Intent.ACTION_SEND)
      .setType("text/plain")
      .putExtra(Intent.EXTRA_TEXT, logSnippet)

    startActivityChooser(shareIntent, resources.getString(R.string.log_view_menu_share_log))
  }

  @Suppress("UNUSED_PARAMETER")
  fun toggleLogging(m: MenuItem) =
    enableLogging(!viewModel.logging)

  private fun enableLogging(enable: Boolean) {
    setLoggingStateSubtitle(enable)
    setPauseButtonState(!enable)
    enableScrolling(!enable)
    viewModel.logging = enable

    if (enable)
      viewModel.log.observe(this, logObserver)
    else
      viewModel.log.removeObservers(this)
  }

  private fun showLog(logLines: List<String>?) {
    val logSnippet = logLines?.joinToString("\n\n") ?: ""

    log_view_text.post {
      log_view_text.text = logSnippet
      log_view_frame.scrollToBottom()
    }
  }

  private fun setLoggingStateSubtitle(enabled: Boolean) {
    supportActionBar.subtitle = when (enabled) {
      true -> getString(R.string.log_view_state_level_format, viewModel.logLevelText)
      false -> getString(R.string.log_view_state_paused)
    }
  }

  private fun setPauseButtonState(paused: Boolean) {
    val iconRes = when (paused) {
      true -> R.drawable.ic_pause_circle_filled_primary_24dp
      false -> R.drawable.ic_pause_circle_outline_primary_24dp
    }

    toggleButton?.setIcon(iconRes)
  }

  private fun enableScrolling(enabled: Boolean) {
    if (enabled)
      log_view_frame.setOnTouchListener(null)
    else
      log_view_frame.setOnTouchListener { _, _ -> true }

    log_view_frame.isSmoothScrollingEnabled = enabled
    log_view_text.setTextIsSelectable(enabled)
    log_view_frame.scrollToBottom()
  }

  private fun ScrollView.scrollToBottom() {
    postDelayed({ fullScroll(View.FOCUS_DOWN) }, 50)
  }
}
