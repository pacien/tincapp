/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.commands

import android.os.AsyncTask
import java8.util.concurrent.CompletableFuture
import java8.util.function.Supplier
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

/**
 * @author pacien
 */
internal object Executor {
  class CommandExecutionException(msg: String) : Exception(msg)

  private fun read(stream: InputStream) = BufferedReader(InputStreamReader(stream)).readLines()

  fun run(cmd: Command): Process = try {
    ProcessBuilder(cmd.asList()).start()
  } catch (e: IOException) {
    throw CommandExecutionException(e.message ?: "Could not start process.")
  }

  fun call(cmd: Command): CompletableFuture<List<String>> = run(cmd).let { process ->
    supplyAsyncTask<List<String>> {
      val exitCode = process.waitFor()
      if (exitCode == 0) read(process.inputStream)
      else throw CommandExecutionException(read(process.errorStream).lastOrNull() ?: "Non-zero exit status ($exitCode).")
    }
  }

  fun runAsyncTask(r: () -> Unit) = CompletableFuture.supplyAsync(Supplier(r), AsyncTask.THREAD_POOL_EXECUTOR)!!
  fun <U> supplyAsyncTask(s: () -> U) = CompletableFuture.supplyAsync(Supplier(s), AsyncTask.THREAD_POOL_EXECUTOR)!!
}
