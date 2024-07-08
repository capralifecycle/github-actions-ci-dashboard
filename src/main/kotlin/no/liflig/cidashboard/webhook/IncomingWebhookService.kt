package no.liflig.cidashboard.webhook

import mu.KotlinLogging
import mu.withLoggingContext

class IncomingWebhookService {

  companion object {
    private val log = KotlinLogging.logger {}
  }

  fun handlePing(ping: GitHubWebhookPing) {
    withLoggingContext("webhook.event" to ping.toString()) { log.info { "Got ping from GitHub" } }
  }

  fun handleWorkflowRun(workflowRun: GitHubWebhookWorkflowRun) {}
}
