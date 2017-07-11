package org.pacien.tincapp.data

/**
 * @author pacien
 */
data class CidrAddress(val address: String, val prefix: Int) {

    companion object {

        private val SEPARATOR = "/"

        fun fromSlashSeparated(s: String) = CidrAddress(s.substringBefore(SEPARATOR), Integer.parseInt(s.substringAfter(SEPARATOR)))

    }

    fun toSlashSeparated() = address + SEPARATOR + prefix

}
