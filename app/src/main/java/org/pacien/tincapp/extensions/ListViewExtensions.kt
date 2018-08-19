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

package org.pacien.tincapp.extensions

import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView

/**
 * @author pacien
 */

fun <T> ArrayAdapter<T>.setElements(elements: Collection<T>?) {
  if (elements == null) return

  synchronized(this) {
    setNotifyOnChange(false)
    clear()
    addAll(elements)
    notifyDataSetChanged()
    setNotifyOnChange(true)
  }
}

fun ListView.hideTopSeparator() {
  addHeaderView(View(context), null, false)
}

fun ListView.hideBottomSeparator() {
  addFooterView(View(context), null, false)
}
