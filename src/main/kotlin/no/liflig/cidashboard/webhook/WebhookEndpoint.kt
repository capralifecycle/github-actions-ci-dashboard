package no.liflig.cidashboard.webhook

import no.liflig.logging.getLogger
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Header
import org.http4k.lens.HeaderLens
import org.http4k.lens.nonBlankString

/**
 * HTTP handler for the webhook endpoint where GitHub will POST after GitHub Actions Workflows run.
 */
class WebhookEndpoint(private val incomingWebhookService: IncomingWebhookService) : HttpHandler {
  companion object {
    private val log = getLogger()

    /** E.g. `"workflow_run"`, `"ping"`. */
    private val webhookEventType: HeaderLens<String> =
        Header.nonBlankString().required("X-GitHub-Event", "The event type")
  }

  override fun invoke(request: Request): Response {
    val eventType: String = webhookEventType(request)

    log.trace { "Got webhook payload for $eventType" }

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
          log.error {
            field("request.body", request.bodyString())
            field("request.headers", request.headers.joinToString())
            "Unrecognized event: $eventType"
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
