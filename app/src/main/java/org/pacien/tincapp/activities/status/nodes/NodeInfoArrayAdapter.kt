/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2019 Pacien TRAN-GIRARD
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

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import org.pacien.tincapp.databinding.StatusNodeListItemBinding

/**
 * @author pacien
 */
class NodeInfoArrayAdapter(context: Context, private val onItemClick: (NodeInfo) -> Unit) : ArrayAdapter<NodeInfo>(context, -1) {
  private val layoutInflater = LayoutInflater.from(context)!!

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val binding = when (convertView) {
      null -> StatusNodeListItemBinding.inflate(layoutInflater, parent, false)
      else -> DataBindingUtil.getBinding(convertView)!!
    }

    binding.nodeInfo = getItem(position)
    binding.onClick = onItemClick
    return binding.root
  }
}
