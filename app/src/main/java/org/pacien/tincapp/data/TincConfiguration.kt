package org.pacien.tincapp.data

import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.pacien.tincapp.extensions.ApacheConfiguration.getFile
import java.io.File

/**
 * @author pacien
 */
data class TincConfiguration(val ed25519PrivateKeyFile: File? = null,
                             val privateKeyFile: File? = null) {
  companion object {

    private val KEY_ED25519_PRIVATE_KEY_FILE = "Ed25519PrivateKeyFile"
    private val KEY_PRIVATE_KEY_FILE = "PrivateKeyFile"

    fun fromTincConfiguration(f: File) = fromTincConfiguration(Configurations().properties(f))
    fun fromTincConfiguration(c: Configuration) = TincConfiguration(
      c.getFile(KEY_ED25519_PRIVATE_KEY_FILE),
      c.getFile(KEY_PRIVATE_KEY_FILE))
  }
}
