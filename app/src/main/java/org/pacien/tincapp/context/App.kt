package org.pacien.tincapp.context

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.view.WindowManager
import org.pacien.tincapp.R

/**
 * @author pacien
 */
class App : Application() {
  override fun onCreate() {
    super.onCreate()
    appContext = applicationContext
    handler = Handler()
  }

  companion object {
    private var appContext: Context? = null
    private var handler: Handler? = null

    fun getContext() = appContext!!
    fun getResources() = getContext().resources!!

    fun alert(@StringRes title: Int, msg: String, manualLink: String? = null) = handler!!.post {
      AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog)
        .setTitle(title).setMessage(msg)
        .apply { if (manualLink != null) setNeutralButton(R.string.action_open_manual) { _, _ -> openURL(manualLink) } }
        .setPositiveButton(R.string.action_close, { _, _ -> Unit })
        .create().apply { window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR) }.show()
    }

    fun openURL(url: String) {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      val chooser = Intent.createChooser(intent, getResources().getString(R.string.action_open_web_page))
      appContext?.startActivity(chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
  }
}
