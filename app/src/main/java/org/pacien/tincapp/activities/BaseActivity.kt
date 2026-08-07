/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2025 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import androidx.core.view.WindowInsetsControllerCompat
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppInfo
import org.pacien.tincapp.databinding.BaseActivityBinding

/**
 * @author euxane
 */
abstract class BaseActivity : AppCompatActivity() {
  val rootView by lazy { BaseActivityBinding.inflate(layoutInflater).root }
  private var active = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    super.setContentView(rootView)
    setStatusBarTheme()
  }

  protected fun isLaunchedFromHistory() =
    (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0

  // The launch intent is re-delivered on re-creation and when re-opened from the recent tasks.
  protected fun isFirstIntentDelivery(savedInstanceState: Bundle?) =
    savedInstanceState == null && !isLaunchedFromHistory()

  // Ensure contrast of system status bar text and icons on light background,
  // due to edge-to-edge rendering starting with API 35.
  private fun setStatusBarTheme() {
    val insetsController = WindowInsetsControllerCompat(window, window.decorView)
    insetsController.isAppearanceLightStatusBars = true
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_base, m)
    return true
  }

  override fun onStart() {
    super.onStart()
    active = true
  }

  override fun onResume() {
    super.onResume()
    active = true
  }

  override fun onPause() {
    active = false
    super.onPause()
  }

  override fun onStop() {
    active = false
    super.onStop()
  }

  override fun setContentView(layoutResID: Int) {
    layoutInflater.inflate(layoutResID, rootView)
  }

  override fun getSupportActionBar() = super.getSupportActionBar()!!

  fun startActivityChooser(target: Intent, title: String) {
    val intentChooser = Intent.createChooser(target, title)
    startActivity(intentChooser)
  }

  @Suppress("UNUSED_PARAMETER")
  fun aboutDialog(m: MenuItem) = showAboutDialog()

  fun showAboutDialog() {
    AlertDialog.Builder(this)
      .setTitle(resources.getString(R.string.app_name))
      .setMessage(resources.getString(R.string.about_app_short_desc) + "\n\n" +
        resources.getString(R.string.about_app_copyright) + " " +
        resources.getString(R.string.about_app_license) + "\n\n" +
        AppInfo.all())
      .setNeutralButton(R.string.about_app_open_project_website) { _, _ -> App.openURL(resources.getString(R.string.about_app_website_url)) }
      .setPositiveButton(R.string.generic_action_close) { _, _ -> }
      .show()
  }

  fun runOnUiThread(action: () -> Unit) {
    if (active) super.runOnUiThread(action)
  }

  fun notify(@StringRes msg: Int) = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show()
  fun notify(msg: String) = Snackbar.make(rootView, msg, Snackbar.LENGTH_LONG).show()

  fun showErrorDialog(@StringRes msg: Int, docTopic: String? = null) =
    showErrorDialog(getString(msg), docTopic)

  fun showErrorDialog(msg: String, docTopic: String? = null): AlertDialog =
    AlertDialog.Builder(this)
      .setTitle(R.string.generic_title_error).setMessage(msg)
      .setPositiveButton(R.string.generic_action_close) { _, _ -> }
      .apply {
        if (docTopic != null)
          setNeutralButton(R.string.notification_error_action_open_manual) { _, _ ->
            App.openURL(getString(R.string.app_doc_url_format, docTopic))
        }
      }
      .show()
}
