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

package org.pacien.tincapp

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.utils.TincKeyring
import java.util.regex.Pattern

/** Tools of the network list: passphrase protection, deletion, and configuration directories. */
@RunWith(AndroidJUnit4::class)
class NetworkToolsTest {
  @get:Rule
  val screenshots = Screenshots.Rule()

  private val netName = "testnet"
  private var scenario: ActivityScenario<MainActivity>? = null

  @Before
  fun setUp() {
    Device.prepare()
    Device.reset()
    Networks.create(netName, "android")
    scenario = ActivityScenario.launch(MainActivity::class.java)
  }

  @After
  fun tearDown() {
    scenario?.close()
    Device.reset()
  }

  private fun lock(passphrase: String) {
    clickNetworkMenuItem(netName, R.string.start_network_action_lock)
    typeInto(R.string.decrypt_key_modal_field_passphrase, passphrase)
    click(R.string.configure_tools_lock_network_action)
  }

  private fun unlock(passphrase: String) {
    clickNetworkMenuItem(netName, R.string.start_network_action_unlock)
    typeInto(R.string.decrypt_key_modal_field_passphrase, passphrase)
    click(R.string.configure_tools_unlock_network_action)
  }

  private fun waitForKeysEncrypted(encrypted: Boolean) {
    waitUntil { assertEquals(encrypted, TincKeyring.needsPassphrase(netName)) }
    waitForGone(text(R.string.configure_tools_private_keys_encryption_encrypting))
  }

  private fun waitForFileManager(fileName: String) =
    waitFor(By.pkg(Pattern.compile(".*documentsui")).text(fileName))

  @Test
  fun encryptsAndDecryptsPrivateKeys() {
    lock("correct horse")
    waitForKeysEncrypted(true)

    unlock("correct horse")
    waitForKeysEncrypted(false)
  }

  @Test
  fun rejectsWrongCurrentPassphrase() {
    lock("correct horse")
    waitForKeysEncrypted(true)

    unlock("wrong horse")
    waitFor(text(R.string.generic_title_error))
    Screenshots.take("error")
    assertTrue(TincKeyring.needsPassphrase(netName))
  }

  @Test
  fun deletesNetwork() {
    clickNetworkMenuItem(netName, R.string.start_network_action_delete)
    waitFor(text(R.string.configure_tools_delete_network_message_format, netName))
    Screenshots.take("confirmation")
    click(R.string.configure_tools_delete_network_action)
    waitFor(text(R.string.start_network_list_empty_none_found))
    assertFalse(AppPaths.confDir(netName).exists())
  }

  @Test
  fun opensNetworkDirectoryInFileManager() {
    clickNetworkMenuItem(netName, R.string.start_network_action_edit_config)
    continueWithoutTextEditor()
    waitForFileManager("tinc.conf")
    Screenshots.take("network-directory")
  }

  @Test
  fun opensNetworksDirectoryInFileManager() {
    clickAppMenuItem(R.string.configure_browse_directories_action_open_networks_config)
    continueWithoutTextEditor()
    waitForFileManager(netName)
    Screenshots.take("networks-directory")
  }

  @Test
  fun opensLogsDirectoryInFileManager() {
    clickAppMenuItem(R.string.configure_browse_directories_action_open_logs)
    waitForFileManager("tincapp.log")
    Screenshots.take("logs-directory")
  }

  /** The test system image has no text editor, which the app warns about before opening configuration files. */
  private fun continueWithoutTextEditor() {
    waitFor(text(R.string.configure_browse_directories_warning_no_text_editor))
    Screenshots.take("no-text-editor-warning")
    click(R.string.generic_action_continue)
  }
}
