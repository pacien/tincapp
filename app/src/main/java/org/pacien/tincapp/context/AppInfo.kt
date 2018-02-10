package org.pacien.tincapp.context

import android.os.Build
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R

/**
 * @author pacien
 */
object AppInfo {

  fun appVersion(): String = App.getResources().getString(
    R.string.info_version_format,
    BuildConfig.VERSION_NAME,
    BuildConfig.BUILD_TYPE)

  fun androidVersion(): String = App.getResources().getString(
    R.string.info_running_on_format,
    Build.VERSION.CODENAME,
    Build.VERSION.RELEASE)

  fun supportedABIs(): String = App.getResources().getString(
    R.string.info_supported_abis_format,
    Build.SUPPORTED_ABIS.joinToString(","))

  fun all(): String = listOf(
    appVersion(),
    androidVersion(),
    supportedABIs()).joinToString("\n")

}
