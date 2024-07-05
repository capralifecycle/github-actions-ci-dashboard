package no.liflig.cidashboard.webhook

import no.liflig.cidashboard.common.config.WebhookOptions
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request

class WebhookSecretValidatorFilter(private val secret: WebhookOptions.Secret) : Filter {

  override fun invoke(next: HttpHandler): HttpHandler = { request: Request ->
    // Todo validate secret

    next(request)
  }
}
