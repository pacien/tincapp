/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App

/**
 * Extension functions for [Context] to handle common [Intent] actions.
 */

fun Context.openURL(
  url: String,
  onError: (String) -> Unit = { App.alert(R.string.generic_title_error, it) }
) {
  try {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    val chooser = Intent.createChooser(intent, getString(R.string.generic_action_open_web_page))
    if (this !is Activity) {
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
  } catch (_: ActivityNotFoundException) {
    onError(getString(R.string.generic_error_no_web_browser))
  }
}

fun Context.sendMail(
  recipient: String,
  subject: String,
  body: String,
  onError: (String) -> Unit = { App.alert(R.string.generic_title_error, it) }
) {
  try {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri())
      .putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
      .putExtra(Intent.EXTRA_SUBJECT, subject)
      .putExtra(Intent.EXTRA_TEXT, body)

    val chooser = Intent.createChooser(intent, getString(R.string.crash_modal_action_send_email))
    if (this !is Activity) {
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    startActivity(chooser)
  } catch (_: ActivityNotFoundException) {
    onError(getString(R.string.generic_error_no_email_client))
  }
}
