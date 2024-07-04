package no.liflig.cidashboard.webhook

import mu.KotlinLogging
import mu.withLoggingContext
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class WebhookEndpoint : HttpHandler {
  companion object {
    private val log = KotlinLogging.logger {}
  }

  override fun invoke(request: Request): Response {
    withLoggingContext("request.body" to request.bodyString()) {
      log.info { "Got webhook payload" }
    }

    // Todo handle payload
    return Response(Status.OK)
  }
}
