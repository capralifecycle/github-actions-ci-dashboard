package no.liflig.cidashboard.webhook

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.common.config.WebhookOptions
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

/** Sent when you first register a webhook. */
@Serializable
data class GitHubWebhookPing(val hook: Hook, val organization: Organization?, val sender: Sender) :
    WebhookPayload {
  companion object {
    val bodyLens: BiDiBodyLens<GitHubWebhookPing> = Body.auto<GitHubWebhookPing>().toLens()

    private val json = Json { ignoreUnknownKeys = true }

    fun fromJson(jsonString: String): GitHubWebhookPing = json.decodeFromString(jsonString)
  }

  @Serializable
  data class Hook(
      /** `"Organization"` */
      val type: String,
      /** `488374259` */
      val id: Long,
      /** `["workflow_run"]` */
      val events: List<String>,
      val config: Config
  ) {
    @Serializable
    data class Config(
        /** `"json"` */
        @SerialName("content_type") val contentType: String,
        /** `"********"` */
        @Serializable(with = SecretSerializer::class) val secret: WebhookOptions.Secret
    )
  }

  @Serializable
  data class Organization(
      /** `"capralifecycle"` */
      val login: String,
  )

  @Serializable
  data class Sender(
      /** `"krissrex"` */
      val login: String,
      /** `"User"` */
      val type: String
  )
}

private object SecretSerializer : KSerializer<WebhookOptions.Secret> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("secret", PrimitiveKind.STRING)

  override fun deserialize(decoder: Decoder): WebhookOptions.Secret {
    return decoder.decodeString().let { secretString -> WebhookOptions.Secret(secretString) }
  }

  override fun serialize(encoder: Encoder, value: WebhookOptions.Secret) {
    encoder.encodeString(value.value)
  }
}
