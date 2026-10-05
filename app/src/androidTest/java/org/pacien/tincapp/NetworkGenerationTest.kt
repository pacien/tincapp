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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.context.AppPaths

@RunWith(AndroidJUnit4::class)
class NetworkGenerationTest {
  @get:Rule
  val screenshots = Screenshots.Rule()

  private var scenario: ActivityScenario<MainActivity>? = null

  @Before
  fun setUp() {
    Device.prepare()
    Device.reset()
    scenario = ActivityScenario.launch(MainActivity::class.java)
  }

  @After
  fun tearDown() {
    scenario?.close()
    Device.reset()
  }

  private fun generate(netName: String, nodeName: String) {
    waitFor(text(R.string.start_network_list_empty_none_found))
    clickIcon(R.string.configure_tools_generate_config_title)
    click(R.string.configure_tools_action_create_node_configuration_and_keys)
    typeInto(R.string.configure_tools_generate_config_net_name_hint, netName)
    typeInto(R.string.configure_tools_generate_config_node_name_hint, nodeName)
    click(R.string.configure_tools_generate_config_action)
  }

  @Test
  fun generatesNetworkFromNetworkList() {
    generate("testnet", "android")

    val netDir = AppPaths.confDir("testnet")
    val files = listOf("tinc.conf", "network.conf", "ed25519_key.priv", "rsa_key.priv", "hosts/android")
    waitUntil { files.forEach { assertTrue("$it exists", netDir.resolve(it).isFile) } }
    assertTrue(netDir.resolve("tinc.conf").readText().contains("Name = android"))
    waitForGone(text(R.string.configure_tools_generate_config_title)) // the dialog, also having the name typed in
    waitFor(text("testnet"))
    Screenshots.take("listed")
  }

  @Test
  fun rejectsInvalidNetworkName() {
    generate("test/net", "android")
    waitFor(text(R.string.configure_tools_message_invalid_network_name))
    Screenshots.take("rejected")
    assertEquals(emptyList<String>(), AppPaths.confDir().list()!!.toList())
  }
}
