package no.liflig.cidashboard.admin.database

import no.liflig.cidashboard.persistence.CiStatusId
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Query
import org.http4k.lens.nonBlankString

class DeleteCiStatusEndpoint(private val deleteDatabaseRowsService: DeleteDatabaseRowsService) :
    HttpHandler {

  companion object {
    private val secretQuery = Query.required("secret", "To prevent accidental deletes")
    private val ciStatusIdQuery = Query.nonBlankString().required("id", "CI Status ID to delete")
  }

  override fun invoke(request: Request): Response {
    if (secretQuery(request) != "do-harm") {
      return Response(Status.UNAUTHORIZED).body("Invalid query parameter 'secret'")
    }

    val id = CiStatusId(ciStatusIdQuery(request))
    deleteDatabaseRowsService.deleteById(id)

    return Response(Status.OK)
  }
}
