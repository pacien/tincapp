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

package org.pacien.tincapp.utils

import android.os.ParcelFileDescriptor
import org.pacien.tincapp.commands.TincApp
import java.io.File
import java.io.FileNotFoundException

/**
 * @author pacien
 */
object TincKeyring {
  fun needsPassphrase(netName: String) = try {
    TincApp.listPrivateKeys(netName).filter { it.exists() }.any { PemUtils.isEncrypted(PemUtils.read(it)) }
  } catch (e: FileNotFoundException) {
    false
  }

  fun openPrivateKey(f: File?, passphrase: String?): ParcelFileDescriptor? {
    if (f == null || !f.exists() || passphrase == null) return null
    val pipe = ParcelFileDescriptor.createPipe()
    val decryptedKey = PemUtils.decrypt(PemUtils.read(f), passphrase)
    val outputStream = ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
    PemUtils.write(decryptedKey, outputStream.writer())
    return pipe[0]
  }
}
