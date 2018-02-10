package org.pacien.tincapp.data

import org.apache.commons.configuration2.ex.ConversionException

/**
 * @author pacien
 */
data class CidrAddress(val address: String, val prefix: Int) {

  companion object {

    private val SEPARATOR = "/"

    fun fromSlashSeparated(s: String) = try {
      CidrAddress(s.substringBefore(SEPARATOR), Integer.parseInt(s.substringAfter(SEPARATOR)))
    } catch (e: Exception) {
      throw ConversionException(e.message, e)
    }

  }

  fun toSlashSeparated() = address + SEPARATOR + prefix

}
