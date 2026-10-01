/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2024 Euxane P. TRAN-GIRARD
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
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.activities.common.ProgressModal
import org.pacien.tincapp.activities.configure.tools.ConfigurationToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.ConfigurationToolViewModel
import org.pacien.tincapp.activities.configure.tools.ConfigurationToolViewModel.ActionState
import org.pacien.tincapp.activities.configure.tools.EncryptDecryptPrivateKeysToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.GenerateConfigToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.JoinNetworkToolDialogFragment
import org.pacien.tincapp.databinding.ConfigureToolsFragmentBinding

/**
 * @author euxane
 */
class ToolsFragment : BaseFragment() {
  private val generateConfigTool by lazy { GenerateConfigToolDialogFragment() }
  private val joinNetworkTool by lazy { JoinNetworkToolDialogFragment() }
  private val encryptDecryptPrivateKeysTool by lazy { EncryptDecryptPrivateKeysToolDialogFragment() }
  private val toolViewModel by lazy { ViewModelProvider(requireActivity())[ConfigurationToolViewModel::class.java] }
  private var progressDialog: AlertDialog? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val binding = ConfigureToolsFragmentBinding.inflate(inflater, container, false)
    binding.generateConfigAction = openDialog(generateConfigTool)
    binding.joinNetworkAction = openDialog(joinNetworkTool)
    binding.encryptDecryptPrivateKeysAction = openDialog(encryptDecryptPrivateKeysTool)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    toolViewModel.actionState.observe(viewLifecycleOwner) { onActionStateChanged(it) }
  }

  override fun onDestroyView() {
    dismissProgressDialog()
    super.onDestroyView()
  }

  private fun onActionStateChanged(state: ActionState?) {
    dismissProgressDialog()

    when (state) {
      is ActionState.Running ->
        progressDialog = ProgressModal.show(parentActivity, getString(state.label))

      is ActionState.Succeeded -> {
        parentActivity.notify(R.string.configure_tools_message_network_configuration_written)
        toolViewModel.acknowledgeResult()
      }

      is ActionState.Failed -> {
        parentActivity.showErrorDialog(state.message)
        toolViewModel.acknowledgeResult()
      }

      null -> Unit
    }
  }

  private fun dismissProgressDialog() {
    progressDialog?.dismiss()
    progressDialog = null
  }

  private fun openDialog(tool: ConfigurationToolDialogFragment): () -> Unit =
    { if (!tool.isAdded) tool.show(parentFragmentManager, tool.javaClass.simpleName) }
}
