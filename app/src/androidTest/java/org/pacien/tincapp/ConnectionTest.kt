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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.pacien.tincapp.activities.start.StartActivity
import org.pacien.tincapp.service.TincVpnService
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class ConnectionTest {
  @get:Rule
  val screenshots = Screenshots.Rule()

  private val netName = "testnet"
  private val nodeName = "android"
  private val address = "10.0.0.2"
  private var scenario: ActivityScenario<StartActivity>? = null

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
    if (scenario == null) scenario = ActivityScenario.launch(StartActivity::class.java)
    waitFor(text(netName)).click()
  }

  private fun waitForConnection() = waitFor(text(R.string.status_activity_state_connected_to_format, netName), 60.seconds)

  private fun waitForError() = waitFor(text(R.string.notification_error_title_unable_to_start_tinc), 60.seconds)

  private fun unlock(passphrase: String) {
    waitFor(text(R.string.decrypt_key_modal_title))
    Screenshots.take("passphrase-dialog")
    typeInto("passphrase", passphrase)
    waitFor(text(R.string.decrypt_key_modal_action_unlock)).click()
  }

  @Test
  fun connectsShowsStatusAndDisconnects() {
    Networks.create(netName, nodeName)
    Networks.configureInterface(netName, nodeName, address, "10.0.0.0/24")
    connect()
    waitForConnection()

    waitFor(text("$address/32"))
    Screenshots.take("network")

    waitFor(text(R.string.status_activity_title_node_list)).click()
    waitFor(text(nodeName))
    Screenshots.take("nodes")

    waitFor(text(R.string.status_activity_title_subnet_list)).click()
    waitFor(text(address)) // tinc omits the prefix length of single addresses
    waitFor(text(nodeName))
    Screenshots.take("subnets")

    clickMenuItem(R.string.status_activity_menu_show_log)
    waitFor(textStartingWith(string(R.string.log_view_state_level_format, "")))
    Screenshots.take("log")
    device.pressBack()

    clickMenuItem(R.string.status_activity_menu_disconnect)
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

    connect()
    unlock("correct horse")
    waitForConnection()
  }
}
