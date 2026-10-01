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

package org.pacien.tincapp.activities.configure.tools

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java8.util.concurrent.CompletableFuture
import java8.util.concurrent.CompletionException

/**
 * @author euxane
 */
class ConfigurationToolViewModel : ViewModel() {
  sealed class ActionState {
    data class Running(@StringRes val label: Int) : ActionState()
    object Succeeded : ActionState()
    data class Failed(val message: String) : ActionState()
  }

  private val mutableActionState = MutableLiveData<ActionState?>()
  val actionState: LiveData<ActionState?> = mutableActionState

  fun run(@StringRes label: Int, action: CompletableFuture<Unit>) {
    mutableActionState.value = ActionState.Running(label)
    action.whenComplete { _, exception ->
      mutableActionState.postValue(
        if (exception == null) ActionState.Succeeded
        else ActionState.Failed(errorMessage(exception))
      )
    }
  }

  fun acknowledgeResult() {
    mutableActionState.value = null
  }

  private fun errorMessage(exception: Throwable): String {
    val cause = (exception as? CompletionException)?.cause ?: exception
    return cause.localizedMessage ?: cause.toString()
  }
}
