/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
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

package org.pacien.tincapp.extensions

import java8.util.concurrent.CompletableFuture

/**
 * @author pacien
 */
object Java {

  fun <T> CompletableFuture<T>.exceptionallyAccept(fn: (Throwable) -> Unit) = exceptionally { fn(it); null }!!

  fun <A, R> applyIgnoringException(f: (A) -> R, x: A, alt: R? = null) = try {
    f(x)
  } catch (_: Exception) {
    alt
  }

  fun Throwable.defaultMessage() = this.message ?: "null"

}
