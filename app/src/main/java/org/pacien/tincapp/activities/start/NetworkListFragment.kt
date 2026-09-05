/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2020 Euxane P. TRAN-GIRARD
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.databinding.StartNetworkListBinding
import org.pacien.tincapp.extensions.hideBottomSeparator
import org.pacien.tincapp.extensions.setElements
import org.pacien.tincapp.extensions.updatePlaceholderVisibility

/**
 * @author euxane
 */
class NetworkListFragment : BaseFragment() {
  private val networkListViewModel by lazy { NetworkListViewModel() }
  private val networkListAdapter by lazy { ArrayAdapter<String>(requireContext(), R.layout.start_network_list_item) }
  private lateinit var startNetworkListBinding: StartNetworkListBinding
  var connectToNetworkAction = { _: String -> }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    networkListViewModel.networkList.observe(this) { updateNetworkList(it.orEmpty()) }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    startNetworkListBinding = StartNetworkListBinding.inflate(inflater, container, false)
    return startNetworkListBinding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    val listHeaderView = layoutInflater.inflate(R.layout.start_network_list_header, startNetworkListBinding.startNetworkList, false)
    startNetworkListBinding.startNetworkList.adapter = networkListAdapter
    startNetworkListBinding.startNetworkList.addHeaderView(listHeaderView, null, false)
    startNetworkListBinding.startNetworkList.hideBottomSeparator()
    startNetworkListBinding.startNetworkList.onItemClickListener = AdapterView.OnItemClickListener(this::onItemClick)
  }

  @Suppress("UNUSED_PARAMETER")
  private fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = when (view) {
    is TextView -> connectToNetworkAction(view.text.toString())
    else -> Unit
  }

  private fun updateNetworkList(networks: List<String>) {
    startNetworkListBinding.startNetworkList.updatePlaceholderVisibility(
      startNetworkListBinding.startNetworkListPlaceholder,
      networks.isEmpty()
    )
    networkListAdapter.setElements(networks)
  }
}
