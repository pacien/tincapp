/*
 * tinc app, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2018 Pacien TRAN-GIRARD
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
