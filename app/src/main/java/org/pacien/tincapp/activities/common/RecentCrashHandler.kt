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

package org.pacien.tincapp.activities.common

import androidx.appcompat.app.AlertDialog
import org.apache.commons.io.input.ReversedLinesFileReader
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.context.CrashRecorder
import java.io.File

/**
 * @author pacien
 */
class RecentCrashHandler(private val parentActivity: BaseActivity) {
  private val resources by lazy { parentActivity.resources!! }
  private val maxLines = 1000

  fun handleRecentCrash() {
    if (!CrashRecorder.hasPreviouslyCrashed()) return
    CrashRecorder.dismissPreviousCrash()

    AlertDialog.Builder(parentActivity)
      .setTitle(R.string.crash_modal_title)
      .setMessage(makeMessage())
      .setNeutralButton(R.string.crash_modal_action_send_report) { _, _ -> sendReportMail() }
      .setPositiveButton(R.string.generic_action_close) { _, _ -> Unit }
      .show()
  }

  private fun makeMessage() =
    listOf(
      resources.getString(R.string.crash_modal_message),
      resources.getString(R.string.crash_modal_crash_logged, AppPaths.appLogFile().absolutePath)
    ).joinToString("\n\n")

  private fun sendReportMail() =
    App.sendMail(
      resources.getString(R.string.crash_modal_dev_email),
      listOf(R.string.app_name, R.string.crash_modal_title).joinToString(" / ", transform = resources::getString),
      AppPaths.appLogFile().let { if (it.exists()) it.readLastLines(maxLines) else "" }
    )

  private fun File.readLastLines(n: Int): String {
    val reader = ReversedLinesFileReader(this, Charsets.UTF_8)
    val lastLines = generateSequence(reader::readLine)
      .takeWhile { line: String? -> line != null }
      .take(n)
      .toList()
      .asReversed()

    return lastLines.joinToString("\n")
  }
}
