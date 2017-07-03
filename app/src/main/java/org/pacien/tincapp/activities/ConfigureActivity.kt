package org.pacien.tincapp.activities

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.page_configure.*
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppPaths

/**
 * @author pacien
 */
class ConfigureActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        layoutInflater.inflate(R.layout.page_configure, main_content)
        writeContent()
    }

    private fun writeContent() {
        text_configuration_directory.text = AppPaths.confDir().absolutePath
        text_log_directory.text = AppPaths.cacheDir().absolutePath
        text_tinc_binary.text = AppPaths.tinc().absolutePath
    }

    fun generateConf(@Suppress("UNUSED_PARAMETER") v: View) = notify("Not implemented yet")
    fun joinNetwork(@Suppress("UNUSED_PARAMETER") v: View) = notify("Not implemented yet")

}
