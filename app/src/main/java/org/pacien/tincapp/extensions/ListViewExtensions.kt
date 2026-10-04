/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2018 Euxane P. TRAN-GIRARD
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
 * @author euxane
 */

fun <T> ArrayAdapter<T>.setElements(elements: Collection<T>?) {
  // Leave unchanged lists as they are (avoid view change cancelling pending clicks)
  if (elements == null || elements.toList() == List(count, ::getItem)) return

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

/**
 * Workaround for mishandled list emptyView since Android SDK 34:

java.lang.ClassCastException: android.widget.LinearLayout$LayoutParams cannot be cast to android.widget.AbsListView$LayoutParams
	at android.widget.ListView.removeUnusedFixedViews(ListView.java:2001)
	at android.widget.ListView.layoutChildren(ListView.java:1860)
	at android.widget.AbsListView.onLayout(AbsListView.java:2271)
	at android.widget.AdapterView.updateEmptyStatus(AdapterView.java:794)
	at android.widget.AdapterView.checkFocus(AdapterView.java:767)
	at android.widget.AdapterView$AdapterDataSetObserver.onChanged(AdapterView.java:859)
	at android.widget.AbsListView$AdapterDataSetObserver.onChanged(AbsListView.java:6949)
	at android.database.DataSetObservable.notifyChanged(DataSetObservable.java:38)
	at android.widget.BaseAdapter.notifyDataSetChanged(BaseAdapter.java:54)
	at android.widget.ArrayAdapter.notifyDataSetChanged(ArrayAdapter.java:355)
	at org.pacien.tincapp.extensions.ListViewExtensionsKt.setElements(ListViewExtensions.kt:36)
 */
fun ListView.updatePlaceholderVisibility(placeholderView: View, usePlaceholder: Boolean) {
  post {
    placeholderView.visibility = if (usePlaceholder) View.VISIBLE else View.GONE
    visibility = if (usePlaceholder) View.GONE else View.VISIBLE
  }
}
