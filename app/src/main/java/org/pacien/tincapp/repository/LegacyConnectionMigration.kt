/*
 * Tinc Mesh VPN: Android client and user interface
 * Copyright (C) 2017-2026 Euxane P. TRAN-GIRARD
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

package org.pacien.tincapp.repository

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences

/**
 * Imports the network to restore from the SharedPreferences store used up to v0.47.
 * That store is deleted afterwards to clear the active network's passphrase.
 */
class LegacyConnectionMigration(private val context: Context) : DataMigration<Preferences> {

    private val legacyStore by lazy { context.getSharedPreferences(LEGACY_STORE_NAME, Context.MODE_PRIVATE) }

    override suspend fun shouldMigrate(currentData: Preferences) =
        legacyStore.all.isNotEmpty()

    override suspend fun migrate(currentData: Preferences): Preferences =
        currentData.toMutablePreferences().apply {
            legacyStore.getString(LEGACY_KEY_NET_NAME, null)?.let { this[VpnDataStore.PreferencesKeys.NET_NAME] = it }
        }.toPreferences()

    override suspend fun cleanUp() {
        context.deleteSharedPreferences(LEGACY_STORE_NAME)
    }

    companion object {
        const val LEGACY_STORE_NAME = "org.pacien.tincapp.service"
        const val LEGACY_KEY_NET_NAME = "netname"
    }
}
