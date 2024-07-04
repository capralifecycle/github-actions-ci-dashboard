package no.liflig.cidashboard.common.config

import java.util.Properties
import no.liflig.properties.stringNotEmpty

data class WebhookOptions(val path: String, val secret: Secret) {

  /**
   * Used for a webhook submitter (GitHub) to authenticate itself. Prevents anyone from posting
   * webhook data.
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
            secret = Secret(properties.stringNotEmpty("webhook.secret")))
  }
}
