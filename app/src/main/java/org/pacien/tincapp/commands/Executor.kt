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

  init {
    System.loadLibrary("exec")
  }

  /**
   * @return -1 on error, forked child PID otherwise
   */
  private external fun forkExec(argcv: Array<String>): Int

  private fun read(stream: InputStream) = BufferedReader(InputStreamReader(stream)).readLines()

  fun forkExec(cmd: Command) {
    if (forkExec(cmd.asArray()) == -1) throw CommandExecutionException("Could not fork child process.")
  }

  fun call(cmd: Command): CompletableFuture<List<String>> {
    val proc = try {
      ProcessBuilder(cmd.asList()).start()
    } catch (e: IOException) {
      throw CommandExecutionException(e.message ?: "Could not start process.")
    }

    return supplyAsyncTask<List<String>> {
      if (proc.waitFor() == 0) read(proc.inputStream)
      else throw CommandExecutionException(read(proc.errorStream).lastOrNull() ?: "Non-zero exit status.")
    }
  }

  fun runAsyncTask(r: () -> Unit) = CompletableFuture.runAsync(Runnable(r), AsyncTask.THREAD_POOL_EXECUTOR)!!
  fun <U> supplyAsyncTask(s: () -> U) = CompletableFuture.supplyAsync(Supplier(s), AsyncTask.THREAD_POOL_EXECUTOR)!!

}
