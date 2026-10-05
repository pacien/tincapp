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
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.rules.TestWatcher
import org.junit.runner.Description
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

// UI Automator, which also reaches the system's file manager, and sees Compose screens through their accessibility
// nodes.

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

/** Matches a text field by its label. */
fun field(@StringRes label: Int): BySelector = By.clazz("android.widget.EditText").hasChild(text(label))

/** Matches a clickable list row by the texts of its cells. */
fun row(vararg cells: String): BySelector = cells.fold(By.clickable(true)) { row, cell -> row.hasChild(text(cell)) }

fun waitFor(selector: BySelector, timeout: Duration = 30.seconds): UiObject2 =
  device.wait(Until.findObject(selector), timeout.inWholeMilliseconds)
    ?: run {
      Screenshots.take("failure")
      throw AssertionError("timed out after $timeout waiting for $selector")
    }

fun waitForGone(selector: BySelector, timeout: Duration = 30.seconds) {
  if (!device.wait(Until.gone(selector), timeout.inWholeMilliseconds)) {
    Screenshots.take("failure")
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
      if (System.nanoTime() > deadline) {
        Screenshots.take("failure")
        throw e
      }
      Thread.sleep(250)
    }
  }
}

/** Scrolls the screen's list until the selected element is shown, as lists only lay out their visible items. */
fun scrollTo(selector: BySelector, timeout: Duration = 30.seconds): UiObject2 {
  val deadline = System.nanoTime() + timeout.inWholeNanoseconds
  while (System.nanoTime() < deadline) {
    device.findObject(selector)?.let { return it }
    device.findObject(By.scrollable(true))?.run {
      scrollUntil(Direction.DOWN, Until.findObject(selector))?.let { return it }
      while (scroll(Direction.UP, 1f)) continue
    }
    Thread.sleep(250)
  }
  return waitFor(selector, timeout = 0.seconds)
}

fun click(@StringRes label: Int) = waitFor(text(label)).click()

fun clickIcon(@StringRes description: Int) = waitFor(By.desc(string(description))).click()

/** Clicks an item of the top bar's overflow menu, which follows the menus of the list items. */
fun clickAppMenuItem(@StringRes title: Int) {
  waitFor(By.desc(string(R.string.generic_action_menu)))
  device.findObjects(By.desc(string(R.string.generic_action_menu))).last().click()
  click(title)
}

fun clickNetworkMenuItem(netName: String, @StringRes title: Int) {
  waitFor(text(netName)).parent.findObject(By.desc(string(R.string.generic_action_menu))).click()
  click(title)
}

fun typeInto(@StringRes fieldLabel: Int, value: String) {
  waitFor(field(fieldLabel)).text = value
}

object Device {
  /**
   * Pre-grants the VPN consent, and starts from the home screen.
   * Also stops the file manager, which would otherwise keep showing directories deleted by [reset], and then fall
   * back to another root.
   */
  fun prepare() {
    shell("appops set ${context.packageName} ACTIVATE_VPN allow")
    listOf("com.android.documentsui", "com.google.android.documentsui").forEach { shell("am force-stop $it") }
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

/**
 * Screenshots of the steps of the running test, collected in the app's cache directory for test reports.
 * Test classes register [Screenshots.Rule] to name them after the running test.
 */
object Screenshots {
  private var test = "unknown"
  private var step = 0

  val dir get() = File(context.cacheDir, "test-screenshots").apply { mkdirs() }

  fun take(stepName: String) {
    device.takeScreenshot(File(dir, "$test.${"%02d".format(++step)}-$stepName.png"))
  }

  class Rule : TestWatcher() {
    override fun starting(description: Description) {
      test = "${description.testClass.simpleName}.${description.methodName}"
      step = 0
    }
  }
}
