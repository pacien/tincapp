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

import java.util.*

/**
 * @author pacien
 */
internal class Command(private val cmd: String) {
  private data class Option(val key: String, val value: String?) {
    fun toCommandLineOption(): String = if (value != null) "--$key=$value" else "--$key"
  }

  private val opts: MutableList<Option> = LinkedList()
  private val args: MutableList<String> = LinkedList()

  fun withOption(key: String, value: String? = null): Command {
    this.opts.add(Option(key, value))
    return this
  }

  fun withArguments(vararg args: String): Command {
    this.args.addAll(listOf(*args))
    return this
  }

  fun asList(): List<String> = listOf(cmd) + opts.map { it.toCommandLineOption() } + args
  fun asArray(): Array<String> = this.asList().toTypedArray()
}
