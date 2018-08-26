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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.activities.configure.tools.ConfigurationToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.EncryptDecryptPrivateKeysToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.GenerateConfigToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.JoinNetworkToolDialogFragment
import org.pacien.tincapp.databinding.ConfigureToolsFragmentBinding

/**
 * @author pacien
 */
class ToolsFragment : BaseFragment() {
  private val generateConfigTool by lazy { GenerateConfigToolDialogFragment() }
  private val joinNetworkTool by lazy { JoinNetworkToolDialogFragment() }
  private val encryptDecryptPrivateKeysTool by lazy { EncryptDecryptPrivateKeysToolDialogFragment() }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val binding = ConfigureToolsFragmentBinding.inflate(inflater, container, false)
    binding.generateConfigAction = openDialog(generateConfigTool)
    binding.joinNetworkAction = openDialog(joinNetworkTool)
    binding.encryptDecryptPrivateKeysAction = openDialog(encryptDecryptPrivateKeysTool)
    return binding.root
  }

  private fun openDialog(tool: ConfigurationToolDialogFragment) =
    { tool.show(fragmentManager, tool.javaClass.simpleName) }
}
