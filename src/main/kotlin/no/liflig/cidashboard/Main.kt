package no.liflig.cidashboard

import kotlin.system.exitProcess
import no.liflig.cidashboard.common.config.Config
import no.liflig.logging.getLogger
import org.slf4j.bridge.SLF4JBridgeHandler

private val log = getLogger()

fun main(args: Array<String>) {
  /** Ensures that all uncaught exceptions are logged. */
  Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler)

  try {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    App(Config.load()).start()
  } catch (e: Throwable) {
    log.error(e) { "Application startup failed" }
    exitProcess(status = 1)
  }
}

private object UncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
  private val log = getLogger()

  override fun uncaughtException(thread: Thread, exception: Throwable) {
    try {
      /** Logback includes a "thread_name" field in the log, so we can know which thread threw. */
      log.error(exception) { "Uncaught exception in thread" }
    } catch (_: Throwable) {
      /**
       * Fallback to default behavior of [ThreadGroup.uncaughtException], in case there's an error
       * in our logger.
       */
      System.err.print("""Exception in thread "${thread.name}" """)
      exception.printStackTrace(System.err)
    }
  }
}
