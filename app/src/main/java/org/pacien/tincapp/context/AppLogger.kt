/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
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

package org.pacien.tincapp.context

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Context
import ch.qos.logback.core.FileAppender
import org.slf4j.LoggerFactory

/**
 * @author pacien
 */
object AppLogger {
  private const val LOGCAT_PATTERN = "[%thread] %msg%n%rEx"
  private const val LOGFILE_PATTERN = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n%rEx"

  fun configure() {
    (LoggerFactory.getILoggerFactory() as LoggerContext)
      .apply { reset() }
      .let { loggerContext ->
        (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger)
          .apply {
            addAppender(LogcatAppender()
              .apply { context = loggerContext }
              .apply { encoder = patternEncoder(loggerContext, LOGCAT_PATTERN) }
              .apply { start() })
          }
          .apply {
            addAppender(FileAppender<ILoggingEvent>()
              .apply { context = loggerContext }
              .apply { encoder = patternEncoder(loggerContext, LOGFILE_PATTERN) }
              .apply { file = AppPaths.appLogFile().absolutePath }
              .apply { start() })
          }
      }
  }

  private fun patternEncoder(ctx: Context, pat: String) =
    PatternLayoutEncoder()
      .apply { context = ctx }
      .apply { pattern = pat }
      .apply { start() }
}
