/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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
