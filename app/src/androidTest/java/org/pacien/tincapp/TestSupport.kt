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

import android.os.ParcelFileDescriptor
import androidx.annotation.StringRes
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.service.TincVpnService
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// UI Automator rather than Espresso: Espresso waits for the AsyncTask pool to be idle, which never happens while
// the app runs the tinc daemon.

val context get() = InstrumentationRegistry.getInstrumentation().targetContext
val device: UiDevice get() = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

fun string(@StringRes id: Int, vararg args: Any): String = context.getString(id, *args)

fun shell(command: String): String =
  InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command).let { fd ->
    ParcelFileDescriptor.AutoCloseInputStream(fd).bufferedReader().use { it.readText() }
  }

/** Matches a text, ignoring case as some widgets display their labels capitalised. */
fun text(text: String): BySelector = By.text(Pattern.compile(Pattern.quote(text), Pattern.CASE_INSENSITIVE))

fun text(@StringRes id: Int, vararg args: Any) = text(string(id, *args))

fun textStartingWith(prefix: String): BySelector = By.text(Pattern.compile(Pattern.quote(prefix) + ".*", Pattern.DOTALL))

fun field(id: String): BySelector = By.res(context.packageName, id)

fun waitFor(selector: BySelector, timeout: Duration = 30.seconds): UiObject2 =
  device.wait(Until.findObject(selector), timeout.inWholeMilliseconds)
    ?: throw AssertionError("timed out after $timeout waiting for $selector")

fun waitForGone(selector: BySelector, timeout: Duration = 30.seconds) {
  if (!device.wait(Until.gone(selector), timeout.inWholeMilliseconds)) {
    throw AssertionError("timed out after $timeout waiting for $selector to disappear")
  }
}

/** Retries the given check until it passes, for state updated by background work. */
fun waitUntil(timeout: Duration = 30.seconds, check: () -> Unit) {
  val deadline = System.nanoTime() + timeout.inWholeNanoseconds
  while (true) {
    try {
      return check()
    } catch (e: Throwable) {
      if (System.nanoTime() > deadline) throw e
      Thread.sleep(250)
    }
  }
}

/** Clicks an action bar item, shown as an icon or in the overflow menu depending on the screen width. */
fun clickMenuItem(@StringRes title: Int) {
  device.findObject(By.desc(string(title)))?.click()
    ?: run {
      waitFor(By.desc("More options")).click()
      waitFor(text(title)).click()
    }
}

fun typeInto(fieldId: String, value: String) {
  waitFor(field(fieldId)).text = value
}

object Device {
  /** Pre-grants the VPN consent, and starts from the home screen. */
  fun prepare() {
    shell("appops set ${context.packageName} ACTIVATE_VPN allow")
    device.pressHome()
  }

  /** Disconnects any VPN, and removes all networks and error notifications. */
  fun reset() {
    if (TincVpnService.getCurrentNetName() != null) {
      TincVpnService.disconnect()
      waitUntil { check(TincVpnService.getCurrentNetName() == null) }
    }
    App.notificationManager.dismissAll()
    AppPaths.confDir().deleteRecursively()
  }
}

object Networks {
  /** Creates a network configuration as the configuration screen's generation tool does. */
  fun create(netName: String, nodeName: String, passphrase: String? = null) {
    Tinc.init(netName, nodeName)
      .thenCompose { TincApp.removeScripts(netName) }
      .thenCompose { TincApp.generateIfaceCfgTemplate(netName) }
      .thenCompose { TincApp.setPassphrase(netName, newPassphrase = passphrase) }
      .get(30, TimeUnit.SECONDS)
  }

  /** Sets the node's address, as users do by editing the generated templates. */
  fun configureInterface(netName: String, nodeName: String, address: String, route: String) {
    AppPaths.netConfFile(netName).writeText("Address = $address/32\nRoute = $route\n")
    File(AppPaths.hostsDir(netName), nodeName).appendText("Subnet = $address/32\n")
  }
}
