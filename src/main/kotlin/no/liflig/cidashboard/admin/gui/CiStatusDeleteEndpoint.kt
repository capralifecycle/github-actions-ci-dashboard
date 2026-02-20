package no.liflig.cidashboard.admin.gui

import no.liflig.cidashboard.admin.auth.CognitoAuthService
import no.liflig.cidashboard.admin.database.DeleteDatabaseRowsService
import no.liflig.cidashboard.persistence.CiStatusId
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path

class CiStatusDeleteEndpoint(
    private val deleteService: DeleteDatabaseRowsService,
    useHotReload: Boolean,
) : HttpHandler {

  private val idLens = Path.of("id")

  override fun invoke(request: Request): Response {
    CognitoAuthService.requireCognitoUser(request)

    val id = CiStatusId(idLens(request))
    deleteService.deleteById(id)

    return Response(Status.OK)
  }
}
