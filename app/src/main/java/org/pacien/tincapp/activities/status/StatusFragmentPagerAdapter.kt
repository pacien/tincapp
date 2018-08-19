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

package org.pacien.tincapp.activities.status

import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.pacien.tincapp.R
import org.pacien.tincapp.activities.status.networkinfo.NetworkInfoFragment
import org.pacien.tincapp.activities.status.nodes.NodeListFragment
import org.pacien.tincapp.context.App

/**
 * @author pacien
 */
class StatusFragmentPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {
  private val resources by lazy { App.getResources() }
  private val pages = listOf(
    R.string.status_activity_title_network_info to NetworkInfoFragment(),
    R.string.status_activity_title_node_list to NodeListFragment()
  )

  override fun getPageTitle(position: Int) = resources.getString(pages[position].first)!!
  override fun getItem(position: Int) = pages[position].second
  override fun getCount() = pages.size
}
