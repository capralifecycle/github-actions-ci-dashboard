package no.liflig.cidashboard.admin.gui

import no.liflig.cidashboard.admin.auth.CognitoAuthService
import no.liflig.cidashboard.admin.database.DeleteDatabaseRowsService
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.logging.getLogger
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Path

class CiStatusDeleteEndpoint(
    private val deleteService: DeleteDatabaseRowsService,
) : HttpHandler {

  private val logger = getLogger()
  private val idLens = Path.of("id")

  override fun invoke(request: Request): Response {
    val user = CognitoAuthService.requireCognitoUser(request)

    val id = CiStatusId(idLens(request))
    deleteService.deleteById(id)

    logger.info {
      field("user", user.username)
      field("repo.id", id)
      "Repo deleted: $id"
    }

    return Response(Status.OK)
  }
}
