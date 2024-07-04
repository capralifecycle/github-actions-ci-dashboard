package no.liflig.cidashboard.api

import no.liflig.cidashboard.common.config.ApiOptions
import no.liflig.cidashboard.common.config.WebhookOptions
import no.liflig.cidashboard.common.http4k.httpNoServerVersionHeader
import no.liflig.cidashboard.dashboard.DashboardUpdatesEndpoint
import no.liflig.cidashboard.dashboard.IndexEndpoint
import no.liflig.cidashboard.health.HealthEndpoint
import no.liflig.cidashboard.health.HealthService
import no.liflig.cidashboard.webhook.WebhookEndpoint
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.logging.LoggingFilter
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer

/**
 * Api setup containing the following:
 * - Any additional filters, e.g., an auth filter.
 * - Routes
 *
 * Try to keep the more technical stuff/noise in [ApiServer] to avoid code overload.
 */
fun createApiServer(
    options: ApiOptions,
    webhookOptions: WebhookOptions,
    services: ApiServices
): RoutingHttpHandler {
  val basicApiSetup =
      LifligBasicApiSetup(
          logHandler = LoggingFilter.createLogHandler(suppressSuccessfulHealthChecks = true),
          logHttpBody = options.logHttpBody,
          corsPolicy = options.corsPolicy)

  val (coreFilters) = basicApiSetup.create(principalLog = { null })

  return coreFilters.then(
      routes(
          "/" bind Method.GET to IndexEndpoint(),
          "/index.html" bind Method.GET to IndexEndpoint(),
          "/dashboard-updates" bind Method.GET to DashboardUpdatesEndpoint(),
          webhookOptions.path bind Method.POST to WebhookEndpoint(),
          "/health" bind Method.GET to HealthEndpoint(services.healthService),
      ))
}

fun RoutingHttpHandler.asJettyServer(options: ApiOptions): Http4kServer =
    this.asServer(
        Jetty(options.serverPort.value, httpNoServerVersionHeader(options.serverPort.value)))

/** Service registry for creating endpoints. */
data class ApiServices(
    val healthService: HealthService,
)
