package org.pacien.tincapp.extensions

import android.annotation.SuppressLint
import java.io.File

/**
 * @author euxane
 */
object Files {
  fun File.makePrivate() {
    this.setExecutable(this.isDirectory, false)
    this.setReadable(true, true)
    this.setWritable(true, true)

    if (this.isDirectory)
      for (file in this.listFiles()!!)
        file.makePrivate()
  }

  @SuppressLint("SetWorldReadable", "SetWorldWritable")
  fun File.makePublic() {
    this.setExecutable(this.isDirectory, false)
    this.setReadable(true, false)
    this.setWritable(true, false)

    if (this.isDirectory)
      for (file in this.listFiles()!!)
        file.makePublic()
  }

  fun File.isParentOf(childCandidate: File, strict: Boolean = true): Boolean {
    var parentOfChild: File? = childCandidate.canonicalFile

    if (strict)
      parentOfChild = parentOfChild?.parentFile

    while (parentOfChild != null) {
      if (parentOfChild.equals(canonicalFile)) return true
      parentOfChild = parentOfChild.parentFile
    }

    return false
  }

  fun File.pathUnder(parent: File): String {
    if (!parent.isParentOf(this, false))
      throw IllegalArgumentException("File is not under the given parent.")

    return canonicalPath.removePrefix(parent.canonicalPath)
  }
}