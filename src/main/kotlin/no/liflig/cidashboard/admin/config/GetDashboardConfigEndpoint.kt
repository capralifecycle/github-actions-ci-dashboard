package no.liflig.cidashboard.admin.config

import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

/**
 * Returns the config for a specific dashboard, or all dashboard configs if no
 * [DashboardConfigId.optionalQueryLens] is given.
 */
class GetDashboardConfigEndpoint(
    private val configService: DashboardConfigService,
) : HttpHandler {

  override fun invoke(request: Request): Response {
    val id = DashboardConfigId.optionalQueryLens(request)

    if (id != null) {
      val config = configService.getById(id) ?: return Response(Status.NOT_FOUND)
      return Response(Status.OK).with(DashboardConfig.bodyLens of config)
    }

    return Response(Status.OK).with(DashboardConfig.bodyLensOfList of configService.getAll())
  }
}
