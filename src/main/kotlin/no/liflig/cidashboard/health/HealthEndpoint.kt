package no.liflig.cidashboard.health

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

class HealthEndpoint(private val healthService: HealthService) : HttpHandler {

  override fun invoke(request: Request): Response {
    return Response(Status.OK).with(HealthStatus.bodyLens of healthService.status())
  }
}
