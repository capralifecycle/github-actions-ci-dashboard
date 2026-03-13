package no.liflig.cidashboard.admin.gui

import no.liflig.cidashboard.admin.auth.CognitoAuthService
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class AdminIndexEndpoint : HttpHandler {
  override fun invoke(request: Request): Response {
    CognitoAuthService.requireCognitoUser(request)
    return Response(Status.FOUND).header("Location", "/admin/ci-statuses")
  }
}
