package org.pacien.tincapp.commands

import java8.util.concurrent.CompletableFuture
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

        return CompletableFuture.supplyAsync<List<String>> {
            if (proc.waitFor() == 0) read(proc.inputStream)
            else throw CommandExecutionException(read(proc.errorStream).lastOrNull() ?: "Non-zero exit status.")
        }
    }

}
