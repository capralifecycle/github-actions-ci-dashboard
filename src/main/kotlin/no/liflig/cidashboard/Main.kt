package no.liflig.cidashboard

import no.liflig.cidashboard.common.config.Config
import org.slf4j.bridge.SLF4JBridgeHandler

fun main(args: Array<String>) {
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  App(Config.load()).start()
}
