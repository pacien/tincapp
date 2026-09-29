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
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.context.CrashRecorder
import org.pacien.tincapp.intent.sendMail
import java.io.File
import java.io.RandomAccessFile

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
    parentActivity.sendMail(
      resources.getString(R.string.crash_modal_dev_email),
      listOf(R.string.app_name, R.string.crash_modal_title).joinToString(" / ", transform = resources::getString),
      AppPaths.appLogFile().let { if (it.exists()) it.readLastLines(maxLines) else "" }
    )
}

/**
 * Reads the last [n] lines of this file, without going through java.nio.file (API 26+).
 */
internal fun File.readLastLines(n: Int): String =
  RandomAccessFile(this, "r").use { file ->
    val chunk = ByteArray(8192)
    var start = file.length()
    var newlines = 0
    while (start > 0 && newlines <= n) {
      val size = minOf(chunk.size.toLong(), start).toInt()
      start -= size
      file.seek(start)
      file.readFully(chunk, 0, size)
      newlines += (0 until size).count { chunk[it] == '\n'.code.toByte() }
    }

    val tail = ByteArray((file.length() - start).toInt())
    file.seek(start)
    file.readFully(tail)
    String(tail, Charsets.UTF_8).lines().dropLastWhile(String::isEmpty).takeLast(n).joinToString("\n")
  }
