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

package org.pacien.tincapp.activities.common

import androidx.activity.ComponentActivity
import org.apache.commons.io.input.ReversedLinesFileReader
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.context.CrashRecorder
import java.io.File

/**
 * @author euxane
 */
class RecentCrashHandler(private val parentActivity: ComponentActivity) {
  private val resources by lazy { parentActivity.resources!! }
  private val maxLines = 1000

  data class CrashInfo(
    val title: String,
    val message: String,
    val onSendReport: () -> Unit
  )

  fun handleRecentCrash(): CrashInfo? {
    if (!CrashRecorder.hasPreviouslyCrashed()) return null
    CrashRecorder.dismissPreviousCrash()

    return CrashInfo(
      title = resources.getString(R.string.crash_modal_title),
      message = makeMessage(),
      onSendReport = ::sendReportMail
    )
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
    val reader = ReversedLinesFileReader.builder()
      .setFile(this)
      .setCharset(Charsets.UTF_8)
      .get()
    val lastLines = generateSequence(reader::readLine)
      .takeWhile { line: String? -> line != null }
      .take(n)
      .toList()
      .asReversed()

    return lastLines.joinToString("\n")
  }
}
