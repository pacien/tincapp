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
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.context.AppPaths
import kotlin.time.Duration.Companion.seconds

/**
 * Tests with real peers, run by the end-to-end VPN test (tests/vpn),
 * which provides them through instrumentation arguments.
 * Skipped otherwise.
 */
@RunWith(AndroidJUnit4::class)
class PeerNetworkTest {
  @get:Rule
  val screenshots = Screenshots.Rule()

  private val arguments = InstrumentationRegistry.getArguments()
  private val netName = "invited"
  private var scenario: ActivityScenario<MainActivity>? = null

  @Before
  fun setUp() {
    assumeTrue("requires peers", arguments.containsKey("invitationUrl"))
    Device.prepare()
    Device.reset()
  }

  @After
  fun tearDown() {
    scenario?.close()
    Device.reset()
  }

  @Test
  fun joinsThroughInvitationAndReachesPeers() {
    scenario = ActivityScenario.launch(MainActivity::class.java)
    clickIcon(R.string.configure_tools_generate_config_title)
    click(R.string.configure_tools_action_join_network_via_invitation_url)
    typeInto(R.string.configure_tools_generate_config_net_name_hint, netName)
    typeInto(R.string.configure_tools_join_network_field_invitation_url_hint, arguments.getString("invitationUrl")!!)
    click(R.string.configure_tools_join_network_action)
    waitUntil(60.seconds) {
      assertTrue(AppPaths.netConfFile(netName).readText().contains(arguments.getString("address")!!))
    }
    waitForGone(text(R.string.configure_tools_join_network_joining), 60.seconds)
    waitForGone(text(R.string.configure_tools_join_network_title)) // the dialog, also having the name typed in

    waitFor(text(netName)).click()
    waitFor(text(R.string.status_activity_state_connected_to_format, netName), 60.seconds)

    // Updated as the daemon discovers the mesh
    scrollTo(row("computer", string(R.string.status_node_reachability_direct_connection)), 60.seconds)
    scrollTo(row("laptop", string(R.string.status_node_reachability_via_format, "computer")), 60.seconds)
    listOf("computerAddress", "laptopAddress").forEach { scrollTo(text(arguments.getString(it)!!)) }
    Screenshots.take("nodes-and-subnets")

    clickIcon(R.string.status_activity_menu_disconnect)
    waitFor(text(netName))
  }
}
