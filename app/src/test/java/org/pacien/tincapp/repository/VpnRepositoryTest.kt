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

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.pacien.tincapp.context.App

@OptIn(ExperimentalCoroutinesApi::class)
class VpnRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(App)
        every { App.getContext() } returns mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testStateTransitions() {
        val repository = VpnRepository
        
        assertEquals(VpnState.Disconnected, repository.vpnState.value)

        repository.updateState(VpnState.Connecting)
        assertEquals(VpnState.Connecting, repository.vpnState.value)

        repository.updateState(VpnState.Connected("test-net"))
        assertEquals(VpnState.Connected("test-net"), repository.vpnState.value)

        repository.updateState(VpnState.Disconnected)
        assertEquals(VpnState.Disconnected, repository.vpnState.value)
    }
}
