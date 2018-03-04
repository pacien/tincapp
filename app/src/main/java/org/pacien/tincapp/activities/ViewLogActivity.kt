package org.pacien.tincapp.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ScrollView
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.page_viewlog.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Executor
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.service.TincVpnService
import java.util.*
import kotlin.concurrent.timer

/**
 * @author pacien
 */
class ViewLogActivity : BaseActivity() {
  companion object {
    private const val LOG_LINES = 250
    private const val LOG_LEVEL = 5
    private const val NEW_LINE = "\n\n"
    private const val UPDATE_INTERVAL = 250L // ms
  }

  private val log = LinkedList<String>()
  private var logUpdateTimer: Timer? = null
  private var logger: Process? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    layoutInflater.inflate(R.layout.page_viewlog, main_content)
    startLogging()
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_viewlog, m)
    return super.onCreateOptionsMenu(m)
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return true
  }

  override fun onDestroy() {
    stopLogging()
    super.onDestroy()
  }

  fun toggleLogging(menuItem: MenuItem) {
    if (logger == null) {
      startLogging()
      menuItem.setIcon(R.drawable.ic_pause_circle_outline_primary_24dp)
    } else {
      stopLogging()
      menuItem.setIcon(R.drawable.ic_pause_circle_filled_primary_24dp)
    }
  }

  private fun startLogging(level: Int = LOG_LEVEL) {
    disableUserScroll()
    appendLog(resources.getString(R.string.message_log_level_set, level))
    Tinc.log(TincVpnService.getCurrentNetName()!!, level).let { process ->
      logger = process
      Executor.runAsyncTask { captureLog(process) }
    }
    logUpdateTimer = timer(period = UPDATE_INTERVAL, action = { printLog() })
  }

  private fun stopLogging() {
    enableUserScroll()
    logger?.destroy()
    logger = null
    logUpdateTimer?.cancel()
    logUpdateTimer?.purge()
    logUpdateTimer = null
    appendLog(resources.getString(R.string.message_log_paused))
    printLog()
  }

  private fun captureLog(logger: Process) {
    logger.inputStream?.use { inputStream ->
      inputStream.bufferedReader().useLines { lines ->
        lines.forEach { appendLog(it) }
      }
    }
  }

  private fun appendLog(line: String) = synchronized(this) {
    if (log.size >= LOG_LINES) log.removeFirst()
    log.addLast(line)
  }

  private fun printLog() = synchronized(this) {
    log.joinToString(NEW_LINE).let {
      logview_text.post {
        logview_text.text = it
        logview_frame.post { logview_frame.fullScroll(View.FOCUS_DOWN) }
      }
    }
  }

  private fun enableUserScroll() {
    logview_text.setTextIsSelectable(true)
    logview_frame.setState(true)
  }

  private fun disableUserScroll() {
    logview_text.setTextIsSelectable(false)
    logview_frame.setState(false)
  }

  private fun ScrollView.setState(enabled: Boolean) {
    if (enabled) setOnTouchListener(null) else setOnTouchListener { _, _ -> true }
    logview_frame.isSmoothScrollingEnabled = enabled
    logview_frame.isVerticalScrollBarEnabled = enabled
  }
}
