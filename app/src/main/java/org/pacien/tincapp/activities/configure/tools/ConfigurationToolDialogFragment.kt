/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.configure.tools

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import android.view.View
import java8.util.concurrent.CompletableFuture
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseDialogFragment
import org.pacien.tincapp.activities.common.ProgressModal
import org.pacien.tincapp.extensions.Java.exceptionallyAccept
import java.util.regex.Pattern

/**
 * @author pacien
 */
abstract class ConfigurationToolDialogFragment : BaseDialogFragment() {
  private val networkNamePattern by lazy { Pattern.compile("^[^\\x00/]*$") }

  protected fun makeDialog(@LayoutRes layout: Int, @StringRes title: Int, @StringRes applyButton: Int, applyAction: (View) -> Unit) =
    makeDialog(inflate(layout), title, applyButton, applyAction)

  protected fun makeDialog(view: View, @StringRes title: Int, @StringRes applyButton: Int, applyAction: (View) -> Unit) =
    AlertDialog.Builder(parentActivity)
      .setTitle(title)
      .setView(view)
      .setPositiveButton(applyButton) { _, _ -> applyAction(view) }
      .setNegativeButton(R.string.generic_action_cancel) { _, _ -> Unit }
      .create()!!

  protected fun execAction(@StringRes label: Int, action: CompletableFuture<Unit>) {
    ProgressModal.show(parentActivity, getString(label)).let { progressDialog ->
      action
        .whenComplete { _, _ -> progressDialog.dismiss() }
        .thenAccept { parentActivity.notify(R.string.configure_tools_message_network_configuration_written) }
        .exceptionallyAccept { parentActivity.runOnUiThread { parentActivity.showErrorDialog(it.cause!!.localizedMessage!!) } }
    }
  }

  protected fun validateNetName(netName: String): CompletableFuture<Unit> =
    if (networkNamePattern.matcher(netName).matches())
      CompletableFuture.completedFuture(Unit)
    else
      CompletableFuture.failedFuture(IllegalArgumentException(getString(R.string.configure_tools_message_invalid_network_name)))
}
