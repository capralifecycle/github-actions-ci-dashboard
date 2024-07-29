package no.liflig.cidashboard.admin.config

import no.liflig.cidashboard.DashboardConfig
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

/** Used instead of migrating the database during development. */
class DashboardConfigEndpoint(
    private val configService: DashboardConfigService,
) : HttpHandler {

  override fun invoke(request: Request): Response {

    configService.handleListUpdate(DashboardConfig.bodyLensOfList(request))

    return Response(Status.OK)
  }
}
