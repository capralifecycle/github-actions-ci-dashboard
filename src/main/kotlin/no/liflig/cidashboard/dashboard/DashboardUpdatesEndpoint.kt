package no.liflig.cidashboard.dashboard

import java.time.Clock
import java.time.Instant
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.common.config.ClientSecretToken
import no.liflig.cidashboard.persistence.CiStatus
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.HttpMessage
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiLens
import org.http4k.lens.Header
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

/**
 * HTTP handler for the html contents inside the [IndexEndpoint]. This page is polled regularly by
 * the index. This page should only render the HTML DOM inside the statuses div, and not a full
 * webpage with `<html>` or `<body>` etc.
 */
class DashboardUpdatesEndpoint(
    private val dashboardUpdatesService: DashboardUpdatesService,
    private val secretToken: ClientSecretToken,
    useHotReload: Boolean,
    private val clock: Clock = Clock.systemUTC(),
) : HttpHandler {

  private val renderer =
      if (useHotReload) {
        Renderer.hotReloading
      } else {
        Renderer.classpath
      }

  private val bodyLens = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()

  companion object {
    /** This can be used to reload the entire index.html, in case `<head>` was modified etc. */
    private val reloadEntirePageLens: BiDiLens<HttpMessage, Boolean> =
        Header.boolean()
            .defaulted("HX-Refresh", false, "If true, the client will refresh the entire page.")
    private val versionLens: BiDiLens<Request, String?> =
        Query.optional(
            "version",
            "Used to reload the entire page when the frontend is using an old index.html.")

    val tokenLens =
        Query.required(
            "token", "Authorization so strangers don't see our repositories and thus customers.")
  }

  override fun invoke(request: Request): Response {
    val providedToken = tokenLens(request)
    if (providedToken != secretToken.value) {
      return Response(Status.FORBIDDEN).body("Invalid token in query")
    }

    val shouldReload: Boolean = versionLens(request) != Index.LATEST_VERSION

    val dashboardConfigId = DashboardConfigId.queryLens(request)

    val data = dashboardUpdatesService.getUpdatedDashboardData(dashboardConfigId)

    return Response(Status.OK)
        .with(
            bodyLens of
                Dashboard(
                    id = data.config?.id?.value ?: "default",
                    statuses = data.lastBuilds,
                    failedBuilds = data.allFailedBuilds,
                    config = data.config ?: DashboardConfig("default"),
                    now = clock.instant()))
        .with(reloadEntirePageLens of shouldReload)
  }
}

data class Dashboard(
    val id: String,
    val statuses: List<CiStatus>,
    val failedBuilds: List<CiStatus>,
    val now: Instant,
    val config: DashboardConfig,
) : ViewModel {
  override fun template() = "dashboard"
}
