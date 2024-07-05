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

class WebhookEndpoint : HttpHandler {
  companion object {
    private val log = KotlinLogging.logger {}

    /** E.g. `"workflow_run"`, `"ping"`. */
    private val webhookEventType: HeaderLens<String> =
        Header.nonBlankString().required("X-GitHub-Event", "The event type")
    /**
     * Signed with the secret. Uses HMAC with SHA-256.
     *
     * `"sha256=5c5134a624883d7df34eae110bf37f78a0620b159fd884760c40a66a3903293f"`
     */
    private val webhookSignature: HeaderLens<String> =
        Header.nonBlankString()
            .required(
                "X-Hub-Signature-256",
                "The payload signature signed with HMAC SHA-256 and the Webhook Secret")
  }

  override fun invoke(request: Request): Response {
    withLoggingContext(
        "request.body" to request.bodyString(),
        "request.headers" to request.headers.joinToString()) {
          log.info { "Got webhook payload" }
        }

    return Response(Status.OK)

    /*val eventType: String = webhookEventType(request)
    try {
      when (eventType) {
        "ping" -> GitHubWebhookPing.bodyLens(request)
        "workflow_run" -> null
        else -> log.warn { "Unrecognized event: $eventType" }
      }
      // Todo handle payload
    } catch (ex: Throwable) {
      log.error(ex) { "Failed to extract webhook payload for workflow_run" }
    }*/

  }
}

interface WebhookPayload
