/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.viewlog

import android.arch.lifecycle.LiveData
import org.pacien.tincapp.commands.Executor
import org.pacien.tincapp.commands.Tinc
import java.util.*
import kotlin.concurrent.timer

/**
 * @author pacien
 */
class LogLiveData(private val netName: String, private val logLevel: Int, private val logLineSize: Int) : LiveData<List<String>>() {
  private val updateInterval = 250L // milliseconds
  private val executor = Executor
  private val log = LinkedList<String>()
  private var loggerProcess: Process? = null
  private var logUpdateTimer: Timer? = null

  override fun onActive() {
    loggerProcess = startNewLogger()
    logUpdateTimer = timer(period = updateInterval, action = { outputLog() })
  }

  override fun onInactive() {
    loggerProcess?.destroy()
    logUpdateTimer?.apply { cancel() }?.apply { purge() }
  }

  private fun startNewLogger(): Process {
    val newProcess = Tinc.log(netName, logLevel)
    executor.runAsyncTask { captureProcessOutput(newProcess) }
    return newProcess
  }

  private fun captureProcessOutput(process: Process) {
    process.inputStream?.use { inputStream ->
      inputStream.bufferedReader().useLines { lines ->
        lines.forEach { appendToLog(it) }
      }
    }
  }

  private fun appendToLog(line: String) {
    synchronized(log) {
      if (log.size >= logLineSize) log.removeFirst()
      log.addLast(line)
    }
  }

  private fun outputLog() {
    synchronized(log) {
      val logView = ArrayList(log)
      postValue(logView)
    }
  }
}
