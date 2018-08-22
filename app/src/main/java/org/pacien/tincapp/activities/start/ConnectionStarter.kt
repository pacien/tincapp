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

package org.pacien.tincapp.activities.start

import android.net.VpnService
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.base_activity.*
import kotlinx.android.synthetic.main.dialog_decrypt_keys.view.*
import org.pacien.tincapp.R
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.utils.TincKeyring

/**
 * @author pacien
 */
class ConnectionStarter(private val parentActivity: StartActivity) {
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
      return parentActivity.startActivityForResult(permissionRequestIntent, parentActivity.permissionRequestCode)

    if (TincKeyring.needsPassphrase(this.netName!!) && this.passphrase == null)
      return askForPassphrase()

    startVpn(this.netName!!, this.passphrase)
  }

  private fun askForPassphrase() {
    val dialogView = parentActivity.layoutInflater.inflate(R.layout.dialog_decrypt_keys, parentActivity.base_activity_frame, false)

    AlertDialog.Builder(parentActivity)
      .setTitle(R.string.decrypt_key_modal_title)
      .setView(dialogView)
      .setPositiveButton(R.string.decrypt_key_modal_action_unlock) { _, _ -> tryStart(passphrase = dialogView.passphrase.text.toString()) }
      .setNegativeButton(R.string.decrypt_key_modal_action_cancel) { _, _ -> Unit }
      .show()
  }

  private fun startVpn(netName: String, passphrase: String? = null) {
    parentActivity.showConnectProgressDialog()
    TincVpnService.connect(netName, passphrase)
  }
}
