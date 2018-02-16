package org.pacien.tincapp.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.base.*
import org.pacien.tincapp.BuildConfig
import org.pacien.tincapp.R
import org.pacien.tincapp.context.App
import org.pacien.tincapp.context.AppInfo

/**
 * @author pacien
 */
abstract class BaseActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.base)
    setSupportActionBar(toolbar)
  }

  override fun onCreateOptionsMenu(m: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_base, m)
    return true
  }

  fun aboutDialog(@Suppress("UNUSED_PARAMETER") i: MenuItem) {
    AlertDialog.Builder(this)
      .setTitle(BuildConfig.APPLICATION_ID)
      .setMessage(resources.getString(R.string.app_short_desc) + "\n\n" +
        resources.getString(R.string.app_copyright) + " " +
        resources.getString(R.string.app_license) + "\n\n" +
        AppInfo.all())
      .setNeutralButton(R.string.action_open_project_website) { _, _ -> App.openURL(resources.getString(R.string.app_website_url)) }
      .setPositiveButton(R.string.action_close, { _, _ -> Unit })
      .show()
  }

  protected fun notify(@StringRes msg: Int) = Snackbar.make(activity_base, msg, Snackbar.LENGTH_LONG).show()
  protected fun notify(msg: String) = Snackbar.make(activity_base, msg, Snackbar.LENGTH_LONG).show()
  protected fun showProgressDialog(@StringRes msg: Int): ProgressDialog = ProgressDialog.show(this, null, getString(msg), true, false)
  protected fun showErrorDialog(msg: String): AlertDialog = AlertDialog.Builder(this)
    .setTitle(R.string.title_error).setMessage(msg)
    .setPositiveButton(R.string.action_close, { _, _ -> Unit }).show()
}
