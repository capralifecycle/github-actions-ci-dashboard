package no.liflig.cidashboard

import no.liflig.cidashboard.common.config.Config

fun main(args: Array<String>) {
  App(Config.load()).start()
}
