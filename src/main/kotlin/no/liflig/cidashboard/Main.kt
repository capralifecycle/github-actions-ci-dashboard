package no.liflig.cidashboard

import kotlin.system.exitProcess
import no.liflig.cidashboard.common.config.Config
import no.liflig.logging.getLogger
import org.slf4j.bridge.SLF4JBridgeHandler

private val log = getLogger()

fun main(args: Array<String>) {
  /** Ensures that all uncaught exceptions are logged. */
  Thread.setDefaultUncaughtExceptionHandler { _, e: Throwable ->
    log.error(e) { "Uncaught exception in thread" }
  }

  try {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    App(Config.load()).start()
  } catch (e: Throwable) {
    /**
     * If we failed to load config/start the application, we want to log it, to make sure that we
     * don't lose the exception.
     */
    log.error(e) { "Application startup failed" }
    exitProcess(status = 1)
  }
}
