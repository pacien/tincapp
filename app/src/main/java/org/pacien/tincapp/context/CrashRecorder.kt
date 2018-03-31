package org.pacien.tincapp.context

import org.slf4j.Logger

/**
 * @author pacien
 */
class CrashRecorder(private val logger: Logger,
                    private val upstreamCrashHandler: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {

  companion object {
    private val flagFile = AppPaths.crashFlagFile()

    fun hasPreviouslyCrashed() = flagFile.exists()

    fun flagCrash() {
      flagFile.apply { if (!exists()) createNewFile() }
    }

    fun dismissPreviousCrash() {
      flagFile.delete()
    }
  }

  override fun uncaughtException(thread: Thread, throwable: Throwable) {
    logger.error("Fatal application error.", throwable)
    flagCrash()
    upstreamCrashHandler.uncaughtException(thread, throwable)
  }
}
