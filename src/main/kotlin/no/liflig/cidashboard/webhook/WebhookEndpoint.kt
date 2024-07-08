package no.liflig.cidashboard.webhook

import mu.KotlinLogging
import mu.withLoggingContext
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Header
import org.http4k.lens.HeaderLens
import org.http4k.lens.nonBlankString

class WebhookEndpoint(
    private val incomingWebhookService: IncomingWebhookService = IncomingWebhookService()
) : HttpHandler {
  companion object {
    private val log = KotlinLogging.logger {}

    /** E.g. `"workflow_run"`, `"ping"`. */
    private val webhookEventType: HeaderLens<String> =
        Header.nonBlankString().required("X-GitHub-Event", "The event type")
  }

  override fun invoke(request: Request): Response {
    val eventType: String = webhookEventType(request)

    // TODO: stop logging body and headers when we have enough sample data.
    withLoggingContext(
        "request.body" to request.bodyString(),
        "request.headers" to request.headers.joinToString()) {
          log.info { "Got webhook payload for $eventType" }
        }

    try {
      when (eventType) {
        "ping" -> {
          val event = GitHubWebhookPing.bodyLens(request)
          incomingWebhookService.handlePing(event)
        }
        "workflow_run" -> {
          val event = GitHubWebhookWorkflowRun.bodyLens(request)
          incomingWebhookService.handleWorkflowRun(event)
        }
        else -> {
          withLoggingContext(
              "request.body" to request.bodyString(),
              "request.headers" to request.headers.joinToString()) {
                log.error { "Unrecognized event: $eventType" }
              }
        }
      }

      return Response(Status.OK)
    } catch (ex: Throwable) {
      log.error(ex) { "Failed to extract webhook payload for $eventType" }
      return Response(Status.INTERNAL_SERVER_ERROR)
    }
  }
}

/** Marker interface to easily find all payload classes. */
interface WebhookPayload
