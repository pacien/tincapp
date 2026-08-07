/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2018 Euxane P. TRAN-GIRARD
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

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.compose.setContent
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.activities.configure.tools.ConfigurationToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.EncryptDecryptPrivateKeysToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.GenerateConfigToolDialogFragment
import org.pacien.tincapp.activities.configure.tools.JoinNetworkToolDialogFragment
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.ui.theme.TincTheme

/**
 * @author euxane
 */
class ConfigureActivity : BaseActivity() {

  private val generateConfigTool by lazy { GenerateConfigToolDialogFragment() }
  private val joinNetworkTool by lazy { JoinNetworkToolDialogFragment() }
  private val encryptDecryptPrivateKeysTool by lazy { EncryptDecryptPrivateKeysToolDialogFragment() }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      TincTheme {
        ConfigureScreen(
          onOpenDirectory = { openDocumentTree(it) },
          onGenerateConfig = { openDialog(generateConfigTool) },
          onJoinNetwork = { openDialog(joinNetworkTool) },
          onEncryptDecryptKeys = { openDialog(encryptDecryptPrivateKeysTool) },
          onNavigateUp = { finish() }
        )
      }
    }
  }

  private fun openDialog(tool: ConfigurationToolDialogFragment) {
    if (!tool.isAdded) tool.show(supportFragmentManager, tool.javaClass.simpleName)
  }

  private fun openDocumentTree(documentId: String) {
    try {
      BrowseFilesIntents.openDocumentTree(this, documentId)
    } catch (e: ActivityNotFoundException) {
      runOnUiThread {
        showErrorDialog(
          R.string.configure_browse_directories_error_no_file_manager,
          docTopic = "troubleshooting.html#no-file-manager-found",
        )
      }
    }
  }
}
