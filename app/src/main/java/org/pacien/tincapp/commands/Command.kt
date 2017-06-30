package org.pacien.tincapp.commands

import java.util.*

/**
 * @author pacien
 */
internal class Command(private val cmd: String) {

    private data class Option(val key: String, val value: String?) {
        override fun toString(): String = if (value != null) "--$key=$value" else "--$key"
    }

    private val opts: MutableList<Option>
    private val args: MutableList<String>

    init {
        this.opts = LinkedList<Option>()
        this.args = LinkedList<String>()
    }

    fun withOption(key: String, value: String? = null): Command {
        this.opts.add(Option(key, value))
        return this
    }

    fun withArguments(vararg args: String): Command {
        this.args.addAll(Arrays.asList(*args))
        return this
    }

    fun asList(): List<String> = listOf(cmd) + opts.map { it.toString() } + args

    fun asArray(): Array<String> = this.asList().toTypedArray()

}
