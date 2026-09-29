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
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class VpnDataStoreTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testContext: Context = mockk()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    
    private val testDataStore = PreferenceDataStoreFactory.create(
        scope = testScope,
        produceFile = { tmpFolder.newFile("test.preferences_pb") }
    )

    @Test
    fun testSaveAndRetrieveConnection() = runTest {
        val vpnDataStore = VpnDataStore(testContext, testDataStore)
        
        vpnDataStore.saveConnection("my-net")
        
        assertEquals("my-net", vpnDataStore.netName.first())
    }

    @Test
    fun testClearConnection() = runTest {
        val vpnDataStore = VpnDataStore(testContext, testDataStore)
        
        vpnDataStore.saveConnection("my-net")
        vpnDataStore.saveConnection(null)
        
        assertEquals(null, vpnDataStore.netName.first())
    }
}
