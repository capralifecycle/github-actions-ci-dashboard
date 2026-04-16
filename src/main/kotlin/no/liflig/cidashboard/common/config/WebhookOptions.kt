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
    /**
     * Per-client webhook secrets, keyed by client ID (e.g. GitHub organization name like
     * "capralifecycle"). Each client can have its own secret, allowing independent secret rotation
     * without downtime.
     *
     * Clients POST to `/webhook/{clientId}` instead of `/webhook`.
     *
     * See `/docs/webhooks-and-secrets.md`.
     */
    val clientSecrets: Map<String, Secret>,
    val branchWhitelist: BranchWhitelist,
    val workflowNameWhitelist: WorkflowNameWhitelist,
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
    private const val CLIENT_SECRET_PREFIX = "webhook.client."
    private const val CLIENT_SECRET_SUFFIX = ".secret"

    fun from(properties: Properties): WebhookOptions =
        WebhookOptions(
            path = properties.stringNotEmpty("webhook.path"),
            secret = Secret(properties.stringNotEmpty("webhook.secret")),
            clientSecrets = clientSecretsFrom(properties),
            branchWhitelist =
                properties
                    .string("webhook.branchWhitelist")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { BranchWhitelist(it) } ?: BranchWhitelist(emptyList()),
            workflowNameWhitelist =
                properties
                    .string("webhook.workflowNameWhitelist")
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.let { WorkflowNameWhitelist(it) } ?: WorkflowNameWhitelist(emptyList()),
        )

    private fun clientSecretsFrom(properties: Properties): Map<String, Secret> =
        properties
            .stringPropertyNames()
            .filter { it.startsWith(CLIENT_SECRET_PREFIX) && it.endsWith(CLIENT_SECRET_SUFFIX) }
            .associate { key ->
              val clientId =
                  key.removePrefix(CLIENT_SECRET_PREFIX).removeSuffix(CLIENT_SECRET_SUFFIX)
              val secretValue = properties.getProperty(key)
              require(secretValue.isNotBlank()) {
                "Secret for client '$clientId' (property '$key') cannot be blank"
              }
              clientId to Secret(secretValue)
            }
  }
}
