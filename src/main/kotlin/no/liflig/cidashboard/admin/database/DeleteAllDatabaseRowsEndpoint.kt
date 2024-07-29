package no.liflig.cidashboard.admin.database

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Query

/** Used instead of migrating the database during development. */
class DeleteAllDatabaseRowsEndpoint(
    private val deleteAllDatabaseRowsService: DeleteAllDatabaseRowsService
) : HttpHandler {

  companion object {
    private val secretQuery = Query.required("secret", "To prevent accidental nukes")
  }

  override fun invoke(request: Request): Response {
    if (secretQuery(request) != "do-harm") {
      return Response(Status.UNAUTHORIZED).body("Invalid query parameter 'secret'")
    }

    deleteAllDatabaseRowsService.deleteAll()

    return Response(Status.OK)
  }
}
