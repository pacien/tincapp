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

package org.pacien.tincapp.activities

import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author pacien
 */
abstract class BaseDialogFragment : DialogFragment() {
  protected val parentActivity by lazy { activity as BaseActivity }
  // getLayoutInflater() calls onCreateDialog. See https://stackoverflow.com/a/15152788
  private val inflater by lazy { activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater }

  fun inflate(@LayoutRes layout: Int) = inflater.inflate(layout, null)!!
  fun inflate(inflateFunc: (LayoutInflater, ViewGroup?, Boolean) -> View) = inflateFunc(inflater, null, false)
}
