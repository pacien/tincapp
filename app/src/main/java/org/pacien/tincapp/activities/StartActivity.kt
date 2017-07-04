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
import org.pacien.tincapp.service.TincVpnService

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

    override fun onResume() {
        super.onResume()

        if (TincVpnService.isConnected()) startActivity(Intent(this, StatusActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        PromptActivity.requestVpnPermission((view as TextView).text.toString())
    }

    fun openConfigureActivity(@Suppress("UNUSED_PARAMETER") i: MenuItem) {
        startActivity(Intent(this, ConfigureActivity::class.java))
    }

    private fun writeContent() {
        network_list.addHeaderView(layoutInflater.inflate(R.layout.fragment_network_list_header, network_list, false), null, false)
        network_list.addFooterView(View(this), null, false)
        network_list.emptyView = network_list_empty
        network_list.adapter = ArrayAdapter<String>(this, R.layout.fragment_list_item, AppPaths.confDir().list())
        network_list.onItemClickListener = this
    }

}
