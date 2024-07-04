package no.liflig.cidashboard.dashboard

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class IndexEndpoint : HttpHandler {
  override fun invoke(request: Request): Response {
    // Todo render index html
    return Response(Status.OK)
  }
}
