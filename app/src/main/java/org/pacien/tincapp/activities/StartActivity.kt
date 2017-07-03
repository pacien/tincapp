package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.page_start.*
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppPaths

/**
 * @author pacien
 */
class StartActivity : BaseActivity(), AdapterView.OnItemClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.page_start, main_content)
        writeContent()
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_start, m)
        return super.onCreateOptionsMenu(m)
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        PromptActivity.requestVpnPermission((view as TextView).text.toString())
    }

    fun openConfigureActivity(@Suppress("UNUSED_PARAMETER") i: MenuItem) {
        startActivity(Intent(this, ConfigureActivity::class.java))
    }

    private fun writeContent() {
        network_list.addHeaderView(layoutInflater.inflate(R.layout.fragment_network_list_header, network_list, false))
        network_list.addFooterView(View(this))
        network_list.emptyView = layoutInflater.inflate(R.layout.fragment_network_list_empty_placeholder, network_list, false)
        network_list.adapter = ArrayAdapter<String>(this, R.layout.fragment_list_item, AppPaths.confDir().list())
        network_list.onItemClickListener = this
    }

}
