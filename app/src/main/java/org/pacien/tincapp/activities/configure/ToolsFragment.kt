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

package org.pacien.tincapp.activities.configure

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.activities.configure.tools.EncryptDecryptPrivateKeysTool
import org.pacien.tincapp.activities.configure.tools.GenerateConfigTool
import org.pacien.tincapp.activities.configure.tools.JoinNetworkTool
import org.pacien.tincapp.databinding.ConfigureToolsFragmentBinding

/**
 * @author pacien
 */
class ToolsFragment : Fragment() {
  private val parentActivity by lazy { activity as BaseActivity }
  private val generateConfigTool by lazy { GenerateConfigTool(parentActivity) }
  private val joinNetworkTool by lazy { JoinNetworkTool(this, parentActivity) }
  private val encryptDecryptPrivateKeysTool by lazy { EncryptDecryptPrivateKeysTool(parentActivity) }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val binding = ConfigureToolsFragmentBinding.inflate(inflater, container, false)
    binding.generateConfigAction = generateConfigTool::openGenerateConfDialog
    binding.joinNetworkAction = joinNetworkTool::openJoinNetworkDialog
    binding.encryptDecryptPrivateKeysAction = encryptDecryptPrivateKeysTool::openEncryptDecryptPrivateKeyDialog
    return binding.root
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    joinNetworkTool.onActivityResult(requestCode, resultCode, data)
  }
}
