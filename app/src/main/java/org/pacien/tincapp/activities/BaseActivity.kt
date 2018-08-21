/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.*
import kotlinx.android.synthetic.main.base_activity.*
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppInfo
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.context.CrashRecorder

/**
 * @author pacien
 */
abstract class BaseActivity : AppCompatActivity() {
  private val rootView by lazy { base_activity_frame!! }
  private var active = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    super.setContentView(R.layout.base_activity)
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
  fun aboutDialog(m: MenuItem) {
    AlertDialog.Builder(this)
      .setTitle(resources.getString(R.string.app_name))
      .setMessage(resources.getString(R.string.app_short_desc) + "\n\n" +
        resources.getString(R.string.app_copyright) + " " +
        resources.getString(R.string.app_license) + "\n\n" +
        AppInfo.all())
      .setNeutralButton(R.string.action_open_project_website) { _, _ -> App.openURL(resources.getString(R.string.app_website_url)) }
      .setPositiveButton(R.string.action_close) { _, _ -> Unit }
      .show()
  }

  fun runOnUiThread(action: () -> Unit) {
    if (active) super.runOnUiThread(action)
  }

  fun handleRecentCrash() {
    if (!CrashRecorder.hasPreviouslyCrashed()) return
    CrashRecorder.dismissPreviousCrash()

    AlertDialog.Builder(this)
      .setTitle(R.string.title_app_crash)
      .setMessage(listOf(
        resources.getString(R.string.message_app_crash),
        resources.getString(R.string.message_crash_logged, AppPaths.appLogFile().absolutePath)
      ).joinToString("\n\n"))
      .setNeutralButton(R.string.action_send_report) { _, _ ->
        App.sendMail(
          resources.getString(R.string.app_dev_email),
          listOf(R.string.app_name, R.string.title_app_crash).joinToString(" / ", transform = resources::getString),
          AppPaths.appLogFile().let { if (it.exists()) it.readText() else "" })
      }
      .setPositiveButton(R.string.action_close) { _, _ -> Unit }
      .show()
  }

  fun inflate(@LayoutRes layout: Int) = layoutInflater.inflate(layout, rootView, false)!!
  fun inflate(inflateFunc: (LayoutInflater, ViewGroup?, Boolean) -> View) = inflateFunc(layoutInflater, rootView, false)

  fun notify(@StringRes msg: Int) = Snackbar.make(base_activity_frame, msg, Snackbar.LENGTH_LONG).show()
  fun notify(msg: String) = Snackbar.make(base_activity_frame, msg, Snackbar.LENGTH_LONG).show()

  fun showErrorDialog(msg: String): AlertDialog = AlertDialog.Builder(this)
    .setTitle(R.string.title_error).setMessage(msg)
    .setPositiveButton(R.string.action_close) { _, _ -> Unit }.show()
}
