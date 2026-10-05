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
import org.pacien.tincapp.activities.configure.ConfigureActivity
import org.pacien.tincapp.activities.start.StartActivity
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
  private var scenario: ActivityScenario<*>? = null

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

  private fun assertNodeReachability(node: String, reachability: String) =
    waitUntil(60.seconds) { assertTrue("$node $reachability", waitFor(text(node)).parent.hasObject(text(reachability))) }

  @Test
  fun joinsThroughInvitationAndReachesPeers() {
    scenario = ActivityScenario.launch(ConfigureActivity::class.java)
    waitFor(text(R.string.configure_tools_action_join_network_via_invitation_url_qr_code)).click()
    typeInto("net_name", netName)
    typeInto("invitation_url", arguments.getString("invitationUrl")!!)
    waitFor(text(R.string.configure_tools_join_network_action)).click()
    waitUntil(60.seconds) {
      assertTrue(AppPaths.netConfFile(netName).readText().contains(arguments.getString("address")!!))
    }
    waitForGone(text(R.string.configure_tools_join_network_joining), 60.seconds)
    scenario?.close()

    scenario = ActivityScenario.launch(StartActivity::class.java)
    waitFor(text(netName)).click()
    waitFor(text(R.string.status_activity_state_connected_to_format, netName), 60.seconds)

    waitFor(text(R.string.status_activity_title_node_list)).click()
    assertNodeReachability("computer", string(R.string.status_node_reachability_direct_connection))
    assertNodeReachability("laptop", string(R.string.status_node_reachability_via_format, "computer"))
    Screenshots.take("nodes")

    waitFor(text(R.string.status_activity_title_subnet_list)).click()
    listOf("computerAddress", "laptopAddress").forEach { waitFor(text(arguments.getString(it)!!)) }
    Screenshots.take("subnets")

    clickMenuItem(R.string.status_activity_menu_disconnect)
    waitFor(text(netName))
  }
}
