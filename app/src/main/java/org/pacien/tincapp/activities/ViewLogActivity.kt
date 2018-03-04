package org.pacien.tincapp.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.page_viewlog.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Executor
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class ViewLogActivity : BaseActivity() {
  companion object {
    private const val LOG_LEVEL = 5
    private const val NEW_LINE = "\n"
  }

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
      text_log.movementMethod = null
      text_log.setTextIsSelectable(false)
      menuItem.setIcon(R.drawable.ic_pause_circle_outline_primary_24dp)
    } else {
      stopLogging()
      text_log.movementMethod = ScrollingMovementMethod.getInstance()
      text_log.setTextIsSelectable(true)
      menuItem.setIcon(R.drawable.ic_pause_circle_filled_primary_24dp)
    }
  }

  private fun startLogging(level: Int = LOG_LEVEL) {
    text_log.append(NEW_LINE)
    text_log.append(resources.getString(R.string.message_log_level_set, level))
    text_log.append(NEW_LINE)

    Tinc.log(TincVpnService.getCurrentNetName()!!, level).let { process ->
      logger = process
      Executor.runAsyncTask { printLog(process) }
    }
  }

  private fun stopLogging() {
    logger?.destroy()
    logger = null
  }

  private fun printLog(logger: Process) {
    logger.inputStream?.use { inputStream ->
      inputStream.bufferedReader().useLines { lines ->
        lines.forEach {
          text_log.post {
            text_log.append(NEW_LINE)
            text_log.append(it)
            text_log.append(NEW_LINE)
          }
        }
      }
    }
  }
}
