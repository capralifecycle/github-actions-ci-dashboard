package no.liflig.cidashboard.webhook

import java.security.MessageDigest
import no.liflig.cidashboard.common.config.WebhookOptions
import no.liflig.logging.getLogger
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.Header
import org.http4k.lens.HeaderLens
import org.http4k.lens.LensFailure
import org.http4k.lens.nonBlankString
import org.http4k.security.HmacSha256

/**
 * Prevents unsigned webhook requests with a 401 Unauthorized. GitHub will always sign its POST
 * requests with a secret and a header.
 *
 * See `/docs/webhooks-and-secrets.md`.
 */
class WebhookSecretValidatorFilter(private val secret: WebhookOptions.Secret) : Filter {

  companion object {
    private val log = getLogger()

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

  override fun invoke(next: HttpHandler): HttpHandler = { request: Request ->
    try {
      val signature = webhookSignature(request)

      if (verified(secret, signature, request.bodyString())) {
        next(request)
      } else {
        log.warn { "Webhook did not pass signature validation: Invalid signature header value." }
        Response(Status.UNAUTHORIZED).body("Unauthorized")
      }
    } catch (missingHeader: LensFailure) {
      log.warn { "Webhook did not pass signature validation: Missing signature header." }
      Response(Status.UNAUTHORIZED).body("Unauthorized")
    }
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun verified(
      secret: WebhookOptions.Secret,
      signature: String,
      /** Must be decoded using utf-8 */
      requestBody: String
  ): Boolean {
    val selfCalculatedSignature =
        "sha256=" +
            HmacSha256.hmacSHA256(secret.value.toByteArray(Charsets.UTF_8), requestBody)
                .toHexString(HexFormat.Default)

    val untrustedSignature = signature.toByteArray()

    val isValid =
        MessageDigest.isEqual(
            selfCalculatedSignature.toByteArray(Charsets.UTF_8), untrustedSignature)

    return isValid
  }
}
