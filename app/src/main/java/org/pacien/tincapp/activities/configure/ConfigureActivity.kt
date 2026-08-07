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

package org.pacien.tincapp.activities.configure

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseActivity
import org.pacien.tincapp.storageprovider.BrowseFilesIntents
import org.pacien.tincapp.ui.theme.TincTheme

/**
 * @author euxane
 */
class ConfigureActivity : BaseActivity() {

    private val viewModel: ConfigureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TincTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") {
                        ConfigureScreen(
                            onOpenDirectory = { openDocumentTree(it) },
                            onGenerateConfig = { navController.navigate("generate_config") },
                            onJoinNetwork = { navController.navigate("join_network") },
                            onEncryptDecryptKeys = { navController.navigate("encrypt_decrypt_keys") },
                            onNavigateUp = { finish() }
                        )
                    }
                    composable("generate_config") {
                        GenerateConfigScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("join_network") {
                        JoinNetworkScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                    composable("encrypt_decrypt_keys") {
                        EncryptDecryptKeysScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
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
