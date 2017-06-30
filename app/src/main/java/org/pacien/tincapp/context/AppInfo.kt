package org.pacien.tincapp.context

import android.content.res.Resources
import android.os.Build
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R

/**
 * @author pacien
 */
object AppInfo {

    fun appVersion(r: Resources): String = r.getString(
            R.string.info_version_format,
            BuildConfig.VERSION_NAME,
            BuildConfig.BUILD_TYPE)

    fun androidVersion(r: Resources): String = r.getString(
            R.string.info_running_on_format,
            Build.VERSION.CODENAME,
            Build.VERSION.RELEASE)

    fun supportedABIs(r: Resources): String = r.getString(
            R.string.info_supported_abis_format,
            Build.SUPPORTED_ABIS.joinToString(","))

    fun all(r: Resources): String = listOf(
            appVersion(r),
            androidVersion(r),
            supportedABIs(r)).joinToString("\n")

}
