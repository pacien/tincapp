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

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import org.pacien.tincapp.R

/**
 * An indefinite progress dialog replacing the deprecated `android.app.ProgressDialog`.
 *
 * @author pacien
 */
object ProgressModal {
  fun show(context: Activity, text: String): AlertDialog {
    return AlertDialog.Builder(context)
      .setView(newDialogView(context.layoutInflater, text))
      .setCancelable(false)
      .show()
  }

  @SuppressLint("InflateParams")
  private fun newDialogView(inflater: LayoutInflater, text: String): View {
    val view = inflater.inflate(R.layout.common_progress_dialog, null)
    val textView: TextView = view.findViewById(R.id.common_progress_dialog_text)
    textView.text = text
    return view
  }
}
