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
