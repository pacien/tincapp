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

package org.pacien.tincapp.activities.start

import android.net.VpnService
import androidx.appcompat.app.AlertDialog
import android.view.inputmethod.EditorInfo
import org.pacien.tincapp.MainActivity
import org.pacien.tincapp.R
import org.pacien.tincapp.databinding.DialogDecryptKeysBinding
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.utils.TincKeyring
import org.pacien.tincapp.extensions.View.on

/**
 * @author euxane
 */
class ConnectionStarter(private val parentActivity: MainActivity) {
  private var netName: String? = null
  private var passphrase: String? = null
  private var displayStatus = false

  fun displayStatus() = displayStatus

  fun tryStart(netName: String? = null, passphrase: String? = null, displayStatus: Boolean? = null) {
    if (netName != null) this.netName = netName
    this.passphrase = passphrase
    if (displayStatus != null) this.displayStatus = displayStatus

    val permissionRequestIntent = VpnService.prepare(parentActivity)
    if (permissionRequestIntent != null)
      return parentActivity.requestVpnPermission(permissionRequestIntent)

    if (TincKeyring.needsPassphrase(this.netName!!) && this.passphrase == null)
      return askForPassphrase()

    startVpn(this.netName!!, this.passphrase)
  }

  private fun askForPassphrase() {
    val dialogViewBinding = DialogDecryptKeysBinding.inflate(parentActivity.layoutInflater)

    val dialog = AlertDialog.Builder(parentActivity)
      .setTitle(R.string.decrypt_key_modal_title)
      .setView(dialogViewBinding.root)
      .setPositiveButton(R.string.decrypt_key_modal_action_unlock) { _, _ -> tryStart(passphrase = dialogViewBinding.passphrase.text.toString()) }
      .setNegativeButton(R.string.decrypt_key_modal_action_cancel) { _, _ -> }
      .create()

    dialogViewBinding.passphrase.on(EditorInfo.IME_ACTION_DONE) {
      dialog.dismiss()
      tryStart(passphrase = dialogViewBinding.passphrase.text.toString())
    }

    dialog.show()
  }

  private fun startVpn(netName: String, passphrase: String? = null) {
    parentActivity.showConnectProgressDialog()
    TincVpnService.connect(netName, passphrase)
  }
}
