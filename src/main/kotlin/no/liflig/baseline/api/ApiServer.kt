package no.liflig.baseline.api

import no.liflig.baseline.common.config.ApiOptions
import no.liflig.baseline.common.http4k.httpNoServerVersionHeader
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.LifligUserPrincipalLog
import no.liflig.http4k.setup.logging.LoggingFilter
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

/**
 * Api server setup containing the following:
 * - Technical/noisy http4k basic API setup stuff (keeping [Api] clean)
 * - Jetty server settings
 */
class ApiServer(private val options: ApiOptions, services: ApiServices) {
  private val basicApiSetup =
      LifligBasicApiSetup(
          logHandler =
              /**
               * TODO: Override principalLogSerializer if custom one is required. Otherwise,
               *   [LifligUserPrincipalLog] is used as default. If principal is not needed, just use
               *   default. Principal is only logged if set up in [LifligBasicApiSetup].config().
               */
              LoggingFilter.createLogHandler(suppressSuccessfulHealthChecks = true),
          logHttpBody = options.logHttpBody,
          corsPolicy = options.corsPolicy)

  private val api = Api(basicApiSetup, options, services).create()

  fun create(): Http4kServer =
      api.asServer(Jetty(options.serverPort, httpNoServerVersionHeader(options.serverPort)))
}
