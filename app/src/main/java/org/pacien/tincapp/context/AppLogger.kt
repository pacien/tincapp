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
