package no.liflig.cidashboard.dashboard

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

/**
 * HTTP handler for the html contents inside the [IndexEndpoint]. This page is polled regularly by
 * the index. This page should only render the HTML DOM inside the statuses div, and not a full
 * webpage with `<html>` or `<body>` etc.
 */
class DashboardUpdatesEndpoint : HttpHandler {

  /** This can be used to reload the entire index.html, in case `<head>` was modified etc. */
  private val reloadEntirePageLens: BiDiLens<HttpMessage, Boolean> =
      Header.boolean()
          .defaulted("HX-Refresh", false, "If true, the client will refresh the entire page.")

  private val versionLens: BiDiLens<Request, String?> =
      Query.optional(
          "version", "Used to reload the entire page when the frontend is using an old index.html.")

  override fun invoke(request: Request): Response {
    val shouldReload: Boolean = versionLens(request) != Index.LATEST_VERSION

    return Response(Status.OK)
        .body("<div>No dashboards</div>")
        .with(reloadEntirePageLens of shouldReload)
  }
}
