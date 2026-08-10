/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2023 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * @author euxane
 */
object PendingIntentUtils {
  fun getActivity(context: Context, requestCode: Int, intent: Intent, flags: Int): PendingIntent {
    val extraFlags =
      PendingIntent.FLAG_IMMUTABLE
    return PendingIntent.getActivity(context, requestCode, intent, flags or extraFlags)
  }
}
