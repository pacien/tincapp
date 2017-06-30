package org.pacien.tincapp.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
                        AppInfo.all(resources))
                .setNeutralButton(R.string.action_open_project_website) { _, _ -> openWebsite(R.string.app_website_url) }
                .setPositiveButton(R.string.action_close) { _, _ -> /* nop */ }
                .show()
    }

    protected fun openWebsite(@StringRes url: Int) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(resources.getString(url))))
    }

    protected fun notify(@StringRes msg: Int) {
        Snackbar.make(activity_base, msg, Snackbar.LENGTH_LONG).show()
    }

    protected fun copyIntoClipboard(label: String, str: String) {
        val c = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        c.primaryClip = ClipData.newPlainText(label, str)
        notify(R.string.message_text_copied)
    }

}
