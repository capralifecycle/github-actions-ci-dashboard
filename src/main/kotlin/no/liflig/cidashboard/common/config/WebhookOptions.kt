package no.liflig.cidashboard.common.config

import java.util.Properties
import no.liflig.cidashboard.webhook.BranchWhitelist
import no.liflig.cidashboard.webhook.WorkflowNameWhitelist
import no.liflig.properties.string
import no.liflig.properties.stringNotEmpty

data class WebhookOptions(
    /** Full path to webhook. Must start with `/`. */
    val path: String,
    val secret: Secret,
    val branchWhitelist: BranchWhitelist,
    val workflowNameWhitelist: WorkflowNameWhitelist
) {

  init {
    require(path.isNotBlank()) { "Webhook path cannot be blank" }
    require(path.startsWith("/")) { "Webhook path must start with a /" }
  }

  /**
   * Used for a webhook submitter (GitHub) to authenticate itself. Prevents anyone from posting
   * webhook data.
   *
   * Used by GitHub to generate a
   * [HMAC signature header](https://docs.github.com/en/webhooks/webhook-events-and-payloads#delivery-headers).
   *
   * See `/docs/webhooks-and-secrets.md`.
   */
  @JvmInline
  value class Secret(val value: String) {
    init {
      require(value.isNotBlank()) { "Secret cannot be blank" }
    }
  }

  companion object {
    fun from(properties: Properties): WebhookOptions =
        WebhookOptions(
            path = properties.stringNotEmpty("webhook.path"),
            secret = Secret(properties.stringNotEmpty("webhook.secret")),
            branchWhitelist =
                properties
                    .string("webhook.branchWhitelist")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { BranchWhitelist(it) }
                    ?: BranchWhitelist(emptyList()),
            workflowNameWhitelist =
                properties
                    .string("webhook.workflowNameWhitelist")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { WorkflowNameWhitelist(it) }
                    ?: WorkflowNameWhitelist(emptyList()),
        )
  }
}
