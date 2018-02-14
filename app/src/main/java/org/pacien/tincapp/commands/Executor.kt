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

  private const val FAILED = -1

  class CommandExecutionException(msg: String) : Exception(msg)

  init {
    System.loadLibrary("exec")
  }

  /**
   * @return FAILED (-1) on error, forked child PID otherwise
   */
  private external fun forkExec(argcv: Array<String>): Int

  /**
   * @return FAILED (-1) on error, 0 on no-op, the supplied PID otherwise
   */
  private external fun wait(pid: Int): Int

  private fun read(stream: InputStream) = BufferedReader(InputStreamReader(stream)).readLines()

  fun forkExec(cmd: Command): CompletableFuture<Void> {
    val pid = forkExec(cmd.asArray())
    return when (pid) {
      FAILED -> CompletableFuture.failedFuture(CommandExecutionException("Could not fork child process."))
      else -> CompletableFuture.runAsync { wait(pid) }
    }
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
