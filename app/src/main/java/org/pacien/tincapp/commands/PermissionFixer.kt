package org.pacien.tincapp.commands

import android.annotation.SuppressLint
import android.content.Context
import org.pacien.tincapp.context.AppPaths
import java.io.File

/**
 * @author pacien
 */
object PermissionFixer {

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    private fun setAllRWXPermissions(f: File): Boolean =
            f.setReadable(true, false) && f.setWritable(true, false) && f.setExecutable(true, false)

    fun makePrivateDirsPublic(ctx: Context): Boolean = listOf(AppPaths.confDir(ctx), AppPaths.logDir(ctx), AppPaths.pidDir(ctx))
            .map { setAllRWXPermissions(it) }.all { it }

}
