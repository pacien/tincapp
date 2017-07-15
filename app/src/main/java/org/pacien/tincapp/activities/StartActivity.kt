package org.pacien.tincapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.fragment_list_view.*
import kotlinx.android.synthetic.main.fragment_network_list_header.*
import org.pacien.tincapp.R
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.Android.setElements
import org.pacien.tincapp.service.TincVpnService
import org.pacien.tincapp.utils.FileObserver

/**
 * @author pacien
 */
class StartActivity : BaseActivity(), AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private var networkListAdapter: ArrayAdapter<String>? = null
    private var confChangeObserver: FileObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        networkListAdapter = ArrayAdapter<String>(this, R.layout.fragment_list_item)
        confChangeObserver = FileObserver(AppPaths.confDir().absolutePath, FileObserver.CHANGE, { _, _ -> onRefresh() })

        layoutInflater.inflate(R.layout.fragment_list_view, main_content)
        list_wrapper.setOnRefreshListener(this)
        list.addHeaderView(layoutInflater.inflate(R.layout.fragment_network_list_header, list, false), null, false)
        list.addFooterView(View(this), null, false)
        list.adapter = networkListAdapter
        list.onItemClickListener = this
    }

    override fun onCreateOptionsMenu(m: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_start, m)
        return super.onCreateOptionsMenu(m)
    }

    override fun onDestroy() {
        confChangeObserver = null
        networkListAdapter = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onRestart()
        onRefresh()
        confChangeObserver?.startWatching()
    }

    override fun onStop() {
        super.onStop()
        confChangeObserver?.stopWatching()
    }

    override fun onResume() {
        super.onResume()
        if (TincVpnService.isConnected()) openStatusActivity()
    }

    override fun onRefresh() {
        val networks = AppPaths.confDir().list().toList()
        runOnUiThread {
            networkListAdapter?.setElements(networks)
            network_list_placeholder.visibility = if (networkListAdapter?.isEmpty ?: true) View.VISIBLE else View.GONE
            list_wrapper.isRefreshing = false
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
            PromptActivity.requestVpnPermission((view as TextView).text.toString())

    fun openConfigureActivity(@Suppress("UNUSED_PARAMETER") i: MenuItem) =
            startActivity(Intent(this, ConfigureActivity::class.java))

    fun openStatusActivity() =
            startActivity(Intent(this, StatusActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

}
