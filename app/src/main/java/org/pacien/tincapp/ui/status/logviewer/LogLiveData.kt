/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2018 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.ui.status.logviewer

import kotlinx.coroutines.*
import org.pacien.tincapp.ui.common.SelfRefreshingLiveData
import org.pacien.tincapp.commands.Tinc
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author euxane
 */
class LogLiveData(private val netName: String, private val logLevel: Int, private val logLineSize: Int)
  : SelfRefreshingLiveData<List<String>>(250, TimeUnit.MILLISECONDS) {

  private val logScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
  private val log = LinkedList<String>()
  private lateinit var loggerProcess: Process

  override fun onActive() {
    super.onActive()
    loggerProcess = startNewLogger()
  }

  override fun onInactive() {
    loggerProcess.destroy()
    logScope.coroutineContext.cancelChildren()
    super.onInactive()
  }

  override suspend fun onRefresh() {
    synchronized(log) {
      val logView = ArrayList(log)
      postValue(logView)
    }
  }

  private fun startNewLogger(): Process {
    val newProcess = Tinc.log(netName, logLevel)
    logScope.launch(Dispatchers.IO) { captureProcessOutput(newProcess) }
    return newProcess
  }

  private fun captureProcessOutput(process: Process) {
    try {
      process.inputStream?.use { inputStream ->
        inputStream.bufferedReader().useLines { lines ->
          lines.forEach { appendToLog(it) }
        }
      }
    } catch (_: Exception) {
      // IOException is expected when the process is destroyed and streams are closed
    }
  }

  private fun appendToLog(line: String) {
    synchronized(log) {
      if (log.size >= logLineSize) log.removeFirst()
      log.addLast(line)
    }
  }
}
