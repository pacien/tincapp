/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.activities.status.nodes

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.base.*
import kotlinx.android.synthetic.main.status_node_info_dialog.view.*
import kotlinx.android.synthetic.main.status_node_list.*
import org.pacien.tincapp.R
import org.pacien.tincapp.commands.Tinc
import org.pacien.tincapp.extensions.hideBottomSeparator
import org.pacien.tincapp.extensions.hideTopSeparator
import org.pacien.tincapp.extensions.setElements
import org.pacien.tincapp.service.TincVpnService

/**
 * @author pacien
 */
class NodeListFragment : Fragment() {
  private val vpnService = TincVpnService
  private val tincCtl = Tinc
  private val netName by lazy { vpnService.getCurrentNetName()!! }
  private val nodeListViewModel by lazy { ViewModelProviders.of(this).get(NodeListViewModel::class.java) }
  private val nodeListAdapter by lazy { ArrayAdapter<String>(context, R.layout.status_node_list_item) }
  private val nodeListObserver by lazy { Observer<List<String>> { nodeListAdapter.setElements(it) } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    nodeListViewModel.nodeList.observe(this, nodeListObserver)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.status_node_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    status_node_list.hideTopSeparator()
    status_node_list.hideBottomSeparator()
    status_node_list.emptyView = status_node_list_placeholder
    status_node_list.onItemClickListener = AdapterView.OnItemClickListener(this::onItemClick)
    status_node_list.adapter = nodeListAdapter
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = when (view) {
    is TextView -> showNodeInfo(view.text.toString())
    else -> Unit
  }

  private fun showNodeInfo(nodeName: String) {
    val dialogTextView = layoutInflater.inflate(R.layout.status_node_info_dialog, main_content, false)

    AlertDialog.Builder(context!!)
      .setTitle(R.string.status_node_info_dialog_title)
      .setView(dialogTextView)
      .setPositiveButton(R.string.status_node_info_dialog_close_action) { _, _ -> Unit }
      .show()

    tincCtl.info(netName, nodeName).thenAccept { nodeInfo ->
      view?.post { dialogTextView.dialog_node_details.text = nodeInfo }
    }
  }
}
