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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.activities.configure.ConfigureActivity
import org.pacien.tincapp.utils.TincKeyring
import java.util.regex.Pattern

@RunWith(AndroidJUnit4::class)
class ConfigurationScreenTest {
  private val netName = "testnet"
  private var scenario: ActivityScenario<ConfigureActivity>? = null

  @Before
  fun setUp() {
    Device.prepare()
    Device.reset()
    Networks.create(netName, "android")
    scenario = ActivityScenario.launch(ConfigureActivity::class.java)
  }

  @After
  fun tearDown() {
    scenario?.close()
    Device.reset()
  }

  private fun setPassphrase(current: String, new: String) {
    waitFor(text(R.string.configure_tools_action_encrypt_decrypt_private_keys)).click()
    typeInto("enc_dec_net_name", netName)
    typeInto("enc_dec_current_passphrase", current)
    typeInto("enc_dec_new_passphrase", new)
    waitFor(text(R.string.configure_tools_private_keys_encryption_action)).click()
  }

  private fun waitForKeysEncrypted(encrypted: Boolean) {
    waitUntil { assertEquals(encrypted, TincKeyring.needsPassphrase(netName)) }
    waitForGone(text(R.string.configure_tools_private_keys_encryption_encrypting))
  }

  @Test
  fun encryptsAndDecryptsPrivateKeys() {
    setPassphrase(current = "", new = "correct horse")
    waitForKeysEncrypted(true)

    setPassphrase(current = "correct horse", new = "")
    waitForKeysEncrypted(false)
  }

  @Test
  fun rejectsWrongCurrentPassphrase() {
    setPassphrase(current = "", new = "correct horse")
    waitForKeysEncrypted(true)

    setPassphrase(current = "wrong horse", new = "")
    waitFor(text(R.string.generic_title_error))
    assertTrue(TincKeyring.needsPassphrase(netName))
  }

  @Test
  fun opensNetworksDirectoryInFileManager() {
    waitFor(text(R.string.configure_browse_directories_action_open_networks_config)).click()
    waitFor(By.pkg(Pattern.compile(".*documentsui")).text(netName))
  }

  @Test
  fun opensLogsDirectoryInFileManager() {
    waitFor(text(R.string.configure_browse_directories_action_open_logs)).click()
    waitFor(By.pkg(Pattern.compile(".*documentsui")).text("tincapp.log"))
  }
}
