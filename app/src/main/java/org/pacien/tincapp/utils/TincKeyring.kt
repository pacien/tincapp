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

package org.pacien.tincapp.utils

import org.pacien.tincapp.commands.TincApp
import org.pacien.tincapp.context.AppPaths
import java.io.File
import java.io.FileNotFoundException
import java.io.FileWriter

/**
 * @author pacien
 */
object TincKeyring {
  fun needsPassphrase(netName: String) = try {
    TincApp.listPrivateKeys(netName).filter { it.exists() }.any { PemUtils.isEncrypted(PemUtils.read(it)) }
  } catch (e: FileNotFoundException) {
    false
  }

  fun unlockKey(target: String, input: File?, passphrase: String?): File? {
    if (input == null || !input.exists() || passphrase == null) return null
    val decryptedKey = PemUtils.decrypt(PemUtils.read(input), passphrase)
    val decryptedFile = tempKey(target)
    PemUtils.write(decryptedKey, FileWriter(decryptedFile, false))
    return decryptedFile
  }

  private fun tempKey(name: String): File {
    val file = File(AppPaths.internalCacheDir(), name)
    file.createNewFile()
    file.deleteOnExit()
    file.makePrivate()
    return file
  }

  private fun File.makePrivate() {
    this.setExecutable(false, false)
    this.setReadable(true, true)
    this.setWritable(true, true)
  }
}
