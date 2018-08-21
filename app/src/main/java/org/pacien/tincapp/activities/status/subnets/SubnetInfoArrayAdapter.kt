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

import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.pacien.tincapp.databinding.StatusSubnetListItemBinding

/**
 * @author pacien
 */
class SubnetInfoArrayAdapter(context: Context?) : ArrayAdapter<SubnetInfo>(context, -1) {
  private val layoutInflater = LayoutInflater.from(context)!!

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
    val binding = when (convertView) {
      null -> StatusSubnetListItemBinding.inflate(layoutInflater, parent, false)
      else -> DataBindingUtil.getBinding(convertView)!!
    }

    binding.subnetInfo = getItem(position)
    return binding.root
  }
}
