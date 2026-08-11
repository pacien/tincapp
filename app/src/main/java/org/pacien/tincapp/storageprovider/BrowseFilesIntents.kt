/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2024 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.storageprovider

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract.Document
import org.slf4j.LoggerFactory

object BrowseFilesIntents {
  private val logger = LoggerFactory.getLogger(BrowseFilesIntents::class.java)

  fun networkConfigDirDocId(networkName: String?): String =
    when (networkName) {
      null -> FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS
      else -> FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS + "/" + networkName
    }

  fun openDocumentTree(context: Context, documentId: String) =
    openDocumentTree(context, FilesDocumentsProvider.documentUri(documentId))

  private fun openDocumentTree(context: Context, contentUri: Uri) =
    context.startActivity(
      Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(contentUri, Document.MIME_TYPE_DIR)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
      }
    )

  fun hasTextEditor(context: Context): Boolean {
    val uri = FilesDocumentsProvider.documentUri(FilesDocumentsProvider.VIRTUAL_ROOT_NETWORKS)
    val intent = Intent(Intent.ACTION_EDIT).apply {
      setDataAndType(uri, "text/plain")
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val activities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.packageManager.queryIntentActivities(
        intent,
        PackageManager.ResolveInfoFlags.of(0)
      )
    } else {
      @Suppress("DEPRECATION")
      context.packageManager.queryIntentActivities(intent, 0)
    }

    logger.debug("Checking for text editor with intent: {}", intent)
    logger.debug("Found {} activities: {}", activities.size, activities.map { it.activityInfo.packageName })

    return activities.isNotEmpty()
  }
}
