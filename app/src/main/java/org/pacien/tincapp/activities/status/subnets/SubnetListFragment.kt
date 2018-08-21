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

package org.pacien.tincapp.activities.status.subnets

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.status_subnet_list_fragment.*
import org.pacien.tincapp.R
import org.pacien.tincapp.extensions.hideBottomSeparator
import org.pacien.tincapp.extensions.hideTopSeparator
import org.pacien.tincapp.extensions.setElements

/**
 * @author pacien
 */
class SubnetListFragment : Fragment() {
  private val subnetListViewModel by lazy { ViewModelProviders.of(this).get(SubnetListViewModel::class.java) }
  private val subnetListAdapter by lazy { SubnetInfoArrayAdapter(context) }
  private val subnetListObserver by lazy { Observer<List<SubnetInfo>> { subnetListAdapter.setElements(it) } }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    subnetListViewModel.nodeList.observe(this, subnetListObserver)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    return inflater.inflate(R.layout.status_subnet_list_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    status_subnet_list.hideTopSeparator()
    status_subnet_list.hideBottomSeparator()
    status_subnet_list.onItemClickListener = null
    status_subnet_list.adapter = subnetListAdapter
  }
}
