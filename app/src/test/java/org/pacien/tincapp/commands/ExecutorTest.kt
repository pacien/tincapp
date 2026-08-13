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

package org.pacien.tincapp.commands

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExecutorTest {

    @Test
    fun testCallSuspendSuccess() = runTest {
        val cmd = Command("sh").withArguments("-c", "echo hello")
        val result = Executor.callSuspend(cmd)
        assertEquals(listOf("hello"), result)
    }

    @Test(expected = Executor.CommandExecutionException::class)
    fun testCallSuspendFailure() = runTest {
        val cmd = Command("sh").withArguments("-c", "echo error >&2; exit 1")
        Executor.callSuspend(cmd)
    }

    @Test
    fun testCallSuspendCancellation() = runTest {
        val cmd = Command("sleep").withArguments("2")
        val job = launch {
            try {
                Executor.callSuspend(cmd)
            } catch (e: Exception) {
                // Expected to be cancelled
            }
        }
        delay(100) // Give it a moment to start the process
        job.cancel()
        job.join()
        // If it reaches here without hanging, it's at least cancellable.
    }
}
