/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2020 Pacien TRAN-GIRARD
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

import java8.util.concurrent.CompletableFuture
import org.pacien.tincapp.context.AppPaths
import java.io.File

/**
 * @author pacien
 */
object Tincd {
  fun start(netName: String, device: String, ed25519PrivateKey: File? = null, rsaPrivateKey: File? = null): CompletableFuture<Unit> =
    Executor.call(Command(AppPaths.tincd().absolutePath)
      .withOption("no-detach")
      .withOption("config", AppPaths.confDir(netName).absolutePath)
      .withOption("pidfile", AppPaths.pidFile(netName).absolutePath)
      .withOption("logfile", AppPaths.logFile(netName).absolutePath)
      .withOption("option", "DeviceType=fd")
      .withOption("option", "Device=@$device")
      .apply { if (ed25519PrivateKey != null) withOption("option", "Ed25519PrivateKeyFile=${ed25519PrivateKey.absolutePath}") }
      .apply { if (rsaPrivateKey != null) withOption("option", "PrivateKeyFile=${rsaPrivateKey.absolutePath}") }
    ).thenApply { }
}
