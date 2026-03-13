package no.liflig.cidashboard.admin.gui

import no.liflig.cidashboard.admin.auth.CognitoAuthService
import no.liflig.cidashboard.common.config.WebhookOptions
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.template.viewModel

class IntegrationGuideEndpoint(
    private val webhookSecret: WebhookOptions.Secret,
    useHotReload: Boolean,
) : HttpHandler {

  private val renderer = if (useHotReload) AdminRenderer.hotReloading else AdminRenderer.classpath
  private val bodyLens = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()

  override fun invoke(request: Request): Response {
    val user = CognitoAuthService.requireCognitoUser(request)

    val page =
        IntegrationGuidePage(
            title = "Integration Guide",
            user = user,
            activePage = "integration",
            webhookSecret = webhookSecret.value,
        )

    return Response(Status.OK).with(bodyLens of page)
  }
}
