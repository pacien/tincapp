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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vpn_settings")

class VpnDataStore(
    private val context: Context,
    private val dataStore: DataStore<Preferences> = context.dataStore
) {

    private object PreferencesKeys {
        val NET_NAME = stringPreferencesKey("net_name")
        val PASSPHRASE = stringPreferencesKey("passphrase")
    }

    val netName: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.NET_NAME]
        }

    val passphrase: Flow<String?> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PASSPHRASE]
        }

    suspend fun saveConnection(netName: String?, passphrase: String?) {
        dataStore.edit { preferences ->
            if (netName != null) {
                preferences[PreferencesKeys.NET_NAME] = netName
            } else {
                preferences.remove(PreferencesKeys.NET_NAME)
            }

            if (passphrase != null) {
                preferences[PreferencesKeys.PASSPHRASE] = passphrase
            } else {
                preferences.remove(PreferencesKeys.PASSPHRASE)
            }
        }
    }
}
