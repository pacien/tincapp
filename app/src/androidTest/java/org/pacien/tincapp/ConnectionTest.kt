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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.service.TincVpnService
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ConnectionTest {
  @get:Rule
  val screenshots = Screenshots.Rule()

  private val netName = "testnet"
  private val nodeName = "android"
  private val address = "10.0.0.2"
  private var scenario: ActivityScenario<MainActivity>? = null

  @Before
  fun setUp() {
    Device.prepare()
    Device.reset()
  }

  @After
  fun tearDown() {
    scenario?.close()
    Device.reset()
  }

  private fun connect() {
    if (scenario == null) scenario = ActivityScenario.launch(MainActivity::class.java)
    waitFor(text(netName)).click()
  }

  private fun waitForConnection() = waitFor(text(R.string.status_activity_state_connected_to_format, netName), 60.seconds)

  private fun waitForError() = waitFor(text(R.string.notification_error_title_unable_to_start_tinc), 60.seconds)

  private fun dismissError() {
    click(android.R.string.ok)
    waitForGone(text(R.string.notification_error_title_unable_to_start_tinc))
  }

  private fun unlock(passphrase: String) {
    waitFor(text(R.string.decrypt_key_modal_title))
    Screenshots.take("passphrase-dialog")
    typeInto(R.string.decrypt_key_modal_field_passphrase, passphrase)
    click(R.string.decrypt_key_modal_action_unlock)
  }

  /**
   * Taps this single node, drawn at the centre of the graph, until the graph is rendered and shows its details.
   * The graph is a web page, without accessibility nodes to look for.
   */
  private fun showNodeInfoFromGraph() {
    val graph = waitFor(By.clazz("android.webkit.WebView"))
    waitUntil {
      graph.click()
      waitFor(text(R.string.status_node_info_dialog_title), 2.seconds)
    }
    waitFor(text(nodeName))
    Screenshots.take("node-info")
    click(R.string.status_node_info_dialog_close_action)
    waitForGone(text(R.string.status_node_info_dialog_title))
  }

  @Test
  fun connectsShowsStatusAndDisconnects() {
    Networks.create(netName, nodeName)
    Networks.configureInterface(netName, nodeName, address, "10.0.0.0/24")
    connect()
    waitForConnection()

    waitFor(text("$address/32"))
    Screenshots.take("network")

    scrollTo(row(nodeName, string(R.string.status_node_reachability_this_node)))
    scrollTo(text(address)) // subnet, tinc omitting the prefix length of single addresses
    Screenshots.take("nodes-and-subnets")

    click(R.string.status_activity_title_topology)
    showNodeInfoFromGraph()
    Screenshots.take("topology")

    click(R.string.status_activity_title_log_view)
    waitFor(By.text(Pattern.compile(".+\n.+", Pattern.DOTALL))) // some log lines
    Screenshots.take("log")

    clickIcon(R.string.status_activity_menu_disconnect)
    waitFor(text(netName))
    Screenshots.take("disconnected")
    waitUntil { assertNull(TincVpnService.getCurrentNetName()) }
  }

  @Test
  fun reportsUnconfiguredInterface() {
    Networks.create(netName, nodeName)
    connect()
    waitForError()
    Screenshots.take("error")
  }

  @Test
  fun asksForPassphraseOfEncryptedKeys() {
    Networks.create(netName, nodeName, passphrase = "correct horse")
    Networks.configureInterface(netName, nodeName, address, "10.0.0.0/24")

    connect()
    unlock("wrong horse")
    waitForError()
    dismissError()

    connect()
    unlock("correct horse")
    waitForConnection()
  }
}
