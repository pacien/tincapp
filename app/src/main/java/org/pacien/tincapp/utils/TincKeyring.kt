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
