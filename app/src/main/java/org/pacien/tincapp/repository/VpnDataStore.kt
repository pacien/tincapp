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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "vpn_settings",
    produceMigrations = { context -> listOf(LegacyConnectionMigration(context)) }
)

class VpnDataStore(
    context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore
) {

    internal object PreferencesKeys {
        val NET_NAME = stringPreferencesKey("net_name")
    }

    val netName: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NET_NAME]
        }

    suspend fun saveConnection(netName: String?) {
        dataStore.edit { preferences ->
            if (netName != null) {
                preferences[PreferencesKeys.NET_NAME] = netName
            } else {
                preferences.remove(PreferencesKeys.NET_NAME)
            }
        }
    }
}
