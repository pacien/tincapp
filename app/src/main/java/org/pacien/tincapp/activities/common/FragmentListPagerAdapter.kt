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

package org.pacien.tincapp.activities.common

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.pacien.tincapp.activities.BaseFragment
import org.pacien.tincapp.context.App

/**
 * @param pages ordered list of title and fragment pairs
 * @author pacien
 */
class FragmentListPagerAdapter(private val pages: List<Pair<Int, BaseFragment>>,
                               fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

  private val resources by lazy { App.getResources() }

  override fun getPageTitle(position: Int) = resources.getString(pages[position].first)
  override fun getItem(position: Int) = pages[position].second
  override fun getCount() = pages.size
}
