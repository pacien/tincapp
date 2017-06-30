package org.pacien.tincapp.commands

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

/**
 * @author pacien
 */
internal object Executor {

    init {
        System.loadLibrary("exec")
    }

    /**
     * @return -1 on error, forked child PID otherwise
     */
    private external fun forkExec(argcv: Array<String>): Int

    @Throws(IOException::class)
    fun forkExec(cmd: Command) {
        if (forkExec(cmd.asArray()) == -1)
            throw IOException()
    }

    @Throws(IOException::class)
    fun call(cmd: Command): List<String> {
        val proc = ProcessBuilder(cmd.asList()).start()
        val outputReader = BufferedReader(InputStreamReader(proc.inputStream))

        var line: String? = outputReader.readLine()
        val list = LinkedList<String>()
        while (line != null) {
            line = outputReader.readLine()
            list.add(line)
        }

        return list
    }

}
