/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.activities.start

import androidx.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.start_network_list.*
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.context.AppPaths
import org.pacien.tincapp.extensions.hideBottomSeparator
import org.pacien.tincapp.extensions.setElements

/**
 * @author pacien
 */
class NetworkListFragment : BaseFragment() {
  private val networkListViewModel by lazy { NetworkListViewModel() }
  private val networkListAdapter by lazy { ArrayAdapter<String>(requireContext(), R.layout.start_network_list_item) }
  var connectToNetworkAction = { _: String -> Unit }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    networkListViewModel.networkList.observe(this, Observer { updateNetworkList(it.orEmpty()) })
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.start_network_list, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val listHeaderView = layoutInflater.inflate(R.layout.start_network_list_header, start_network_list, false)
    start_network_list.addHeaderView(listHeaderView, null, false)
    start_network_list.hideBottomSeparator()
    start_network_list.emptyView = start_network_list_placeholder
    start_network_list.onItemClickListener = AdapterView.OnItemClickListener(this::onItemClick)
    start_network_list.adapter = networkListAdapter
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = when (view) {
    is TextView -> connectToNetworkAction(view.text.toString())
    else -> Unit
  }

  private fun updateNetworkList(networks: List<String>) {
    networkListAdapter.setElements(networks)
    if (networks.isEmpty()) updatePlaceholder()
  }

  private fun updatePlaceholder() {
    start_network_list_placeholder.post {
      start_network_list_placeholder_text?.text = getString(R.string.start_network_list_empty_none_found)
    }
  }
}
