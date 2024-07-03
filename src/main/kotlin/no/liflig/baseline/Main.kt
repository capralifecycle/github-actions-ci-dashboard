package no.liflig.baseline

import no.liflig.baseline.common.config.Config

fun main(args: Array<String>) {
  App(Config.load()).start()
}
