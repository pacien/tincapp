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
import android.content.SharedPreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.pacien.tincapp.repository.LegacyConnectionMigration.Companion.LEGACY_KEY_NET_NAME
import org.pacien.tincapp.repository.LegacyConnectionMigration.Companion.LEGACY_STORE_NAME

@OptIn(ExperimentalCoroutinesApi::class)
class LegacyConnectionMigrationTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private fun contextWithLegacyStore(entries: Map<String, String>): Context {
        val legacyStore = mockk<SharedPreferences> {
            every { all } returns entries
            every { getString(any(), any()) } answers { entries[firstArg()] ?: secondArg() }
        }

        return mockk {
            every { getSharedPreferences(LEGACY_STORE_NAME, Context.MODE_PRIVATE) } returns legacyStore
            every { deleteSharedPreferences(LEGACY_STORE_NAME) } returns true
        }
    }

    private fun migratedDataStore(context: Context) = PreferenceDataStoreFactory.create(
        migrations = listOf(LegacyConnectionMigration(context)),
        scope = TestScope(UnconfinedTestDispatcher()),
        produceFile = { tmpFolder.newFile("test.preferences_pb") }
    )

    @Test
    fun migratesNetNameAndDeletesLegacyStore() = runTest {
        val context = contextWithLegacyStore(mapOf(LEGACY_KEY_NET_NAME to "my-net", "passphrase" to "my-pass"))
        val dataStore = migratedDataStore(context)

        assertEquals("my-net", VpnDataStore(context, dataStore).netName.first())
        assertNull(dataStore.data.first()[stringPreferencesKey("passphrase")])
        verify(exactly = 1) { context.deleteSharedPreferences(LEGACY_STORE_NAME) }
    }

    @Test
    fun deletesLegacyStoreWithoutNetName() = runTest {
        val context = contextWithLegacyStore(mapOf("passphrase" to "my-pass"))
        val dataStore = migratedDataStore(context)

        assertNull(VpnDataStore(context, dataStore).netName.first())
        assertEquals(0, dataStore.data.first().asMap().size)
        verify(exactly = 1) { context.deleteSharedPreferences(LEGACY_STORE_NAME) }
    }

    @Test
    fun skipsMigrationWithoutLegacyStore() = runTest {
        val context = contextWithLegacyStore(emptyMap())
        val dataStore = migratedDataStore(context)

        assertNull(VpnDataStore(context, dataStore).netName.first())
        verify(exactly = 0) { context.deleteSharedPreferences(any()) }
    }
}
