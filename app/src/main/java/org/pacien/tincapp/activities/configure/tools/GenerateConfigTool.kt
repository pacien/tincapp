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

package org.pacien.tincapp.activities.configure.tools

import kotlinx.android.synthetic.main.configure_tools_dialog_network_generate.view.*
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp

/**
 * @author pacien
 */
class GenerateConfigTool(parentActivity: BaseActivity) : ConfigurationTool(parentActivity) {
  fun openGenerateConfDialog() =
    showDialog(
      R.layout.configure_tools_dialog_network_generate,
      R.string.configure_tools_generate_config_title,
      R.string.configure_tools_generate_config_action
    ) { dialog ->
      generateConf(
        dialog.new_net_name.text.toString(),
        dialog.new_node_name.text.toString(),
        dialog.new_passphrase.text.toString()
      )
    }

  private fun generateConf(netName: String, nodeName: String, passphrase: String? = null) = execAction(
    R.string.configure_tools_generate_config_generating,
    validateNetName(netName)
      .thenCompose { Tinc.init(netName, nodeName) }
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.generateIfaceCfgTemplate(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) })
}
