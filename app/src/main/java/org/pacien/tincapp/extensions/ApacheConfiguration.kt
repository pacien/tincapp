package org.pacien.tincapp.extensions

import org.apache.commons.configuration2.Configuration
import org.pacien.tincapp.data.CidrAddress
import java.io.File

/**
 * @author pacien
 */
object ApacheConfiguration {

    fun Configuration.getStringList(key: String): List<String> = getList(String::class.java, key, emptyList())
    fun Configuration.getCidrList(key: String): List<CidrAddress> = getStringList(key).map { CidrAddress.fromSlashSeparated(it) }
    fun Configuration.getIntList(key: String): List<Int> = getList(Int::class.java, key, emptyList())

    fun Configuration.getFile(key: String): File? = getString(key)?.let { File(it) }

}
