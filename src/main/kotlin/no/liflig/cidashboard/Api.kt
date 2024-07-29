package no.liflig.cidashboard

import no.liflig.cidashboard.admin.config.DashboardConfigEndpoint
import no.liflig.cidashboard.admin.config.DashboardConfigService
import no.liflig.cidashboard.admin.database.DeleteAllDatabaseRowsEndpoint
import no.liflig.cidashboard.admin.database.DeleteAllDatabaseRowsService
import no.liflig.cidashboard.common.config.ApiOptions
import no.liflig.cidashboard.common.config.WebhookOptions
import no.liflig.cidashboard.common.http4k.httpNoServerVersionHeader
import no.liflig.cidashboard.dashboard.DashboardUpdatesEndpoint
import no.liflig.cidashboard.dashboard.DashboardUpdatesService
import no.liflig.cidashboard.dashboard.IndexEndpoint
import no.liflig.cidashboard.health.HealthEndpoint
import no.liflig.cidashboard.health.HealthService
import no.liflig.cidashboard.status_api.FetchStatusesEndpoint
import no.liflig.cidashboard.status_api.FilteredStatusesService
import no.liflig.cidashboard.webhook.IncomingWebhookService
import no.liflig.cidashboard.webhook.WebhookEndpoint
import no.liflig.cidashboard.webhook.WebhookSecretValidatorFilter
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.logging.LoggingFilter
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.core.Method
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.routing.webJars
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

  val coreFilters =
      basicApiSetup.create(principalLog = { null }).coreFilters.then(ServerFilters.GZip())

  val indexEndpoint =
      IndexEndpoint(options.clientSecretToken, options.hotReloadTemplates, options.updatesPollRate)
  return coreFilters.then(
      routes(
          "/" bind Method.GET to indexEndpoint,
          "/index.html" bind Method.GET to indexEndpoint,
          "/dashboard-updates" bind
              Method.GET to
              DashboardUpdatesEndpoint(
                  services.dashboardUpdatesService,
                  options.clientSecretToken,
                  options.hotReloadTemplates),
          webhookOptions.path bind
              Method.POST to
              WebhookSecretValidatorFilter(webhookOptions.secret)
                  .then(WebhookEndpoint(services.incomingWebhookService)),
          "/health" bind Method.GET to HealthEndpoint(services.healthService),
          "/api/statuses" bind
              Method.GET to
              ServerFilters.BearerAuth(options.devtoolSecretToken.value)
                  .then(FetchStatusesEndpoint(services.filteredStatusesService)),
          "/admin/nuke" bind
              Method.POST to
              DeleteAllDatabaseRowsEndpoint(services.deleteAllDatabaseRowsService),
          "/admin/config" bind
              Method.POST to
              ServerFilters.BearerAuth(token = options.adminSecretToken.value)
                  .then(DashboardConfigEndpoint(services.dashboardConfigService)),
          static(Classpath("/static")),
          webJars()))
}

fun RoutingHttpHandler.asJettyServer(options: ApiOptions): Http4kServer =
    this.asServer(
        Jetty(options.serverPort.value, httpNoServerVersionHeader(options.serverPort.value)))

/** Service registry for creating endpoints. */
data class ApiServices(
    val healthService: HealthService,
    val incomingWebhookService: IncomingWebhookService,
    val dashboardUpdatesService: DashboardUpdatesService,
    val dashboardConfigService: DashboardConfigService,
    val deleteAllDatabaseRowsService: DeleteAllDatabaseRowsService,
    val filteredStatusesService: FilteredStatusesService
)
