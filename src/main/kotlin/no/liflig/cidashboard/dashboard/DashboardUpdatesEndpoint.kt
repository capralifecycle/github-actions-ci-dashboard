package no.liflig.cidashboard.dashboard

import no.liflig.cidashboard.common.http4k.Renderer
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
    val dashboardUpdatesService: DashboardUpdatesService,
    useHotReload: Boolean
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

    val dashboardIdLens = Query.optional("dashboardId", "Id to identify which config to use")
  }

  override fun invoke(request: Request): Response {
    val shouldReload: Boolean = versionLens(request) != Index.LATEST_VERSION

    val dashboardId = dashboardIdLens(request)

    val statuses = dashboardUpdatesService.handleDashboardUpdate(dashboardId)

    return Response(Status.OK)
        .with(bodyLens of Dashboard(dashboardId.toString(), statuses))
        .with(reloadEntirePageLens of shouldReload)
  }
}

data class Dashboard(val dashboardId: String, val statuses: List<CiStatus>) : ViewModel {
  override fun template() = "dashboard"
}
