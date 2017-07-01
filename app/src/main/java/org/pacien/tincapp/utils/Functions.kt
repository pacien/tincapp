package org.pacien.tincapp.utils

/**
 * @author pacien
 */

fun <A, R> applyIgnoringException(f: (A) -> R, x: A, alt: R? = null) = try {
    f(x)
} catch (_: Exception) {
    alt
}
