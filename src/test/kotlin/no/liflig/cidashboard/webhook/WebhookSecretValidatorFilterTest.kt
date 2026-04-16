package no.liflig.cidashboard.webhook

import java.util.concurrent.atomic.AtomicBoolean
import no.liflig.cidashboard.common.config.WebhookOptions
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Headers
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.security.HmacSha256
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WebhookSecretValidatorFilterTest {

  private val secret = WebhookOptions.Secret("It's a Secret to Everybody")

  private fun nextHandlerSpy(): Pair<AtomicBoolean, HttpHandler> {
    val called = AtomicBoolean(false)
    val handler: HttpHandler = { _ ->
      called.set(true)
      Response(Status.OK)
    }
    return called to handler
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun sign(body: String, secret: WebhookOptions.Secret): String =
      "sha256=" +
          HmacSha256.hmacSHA256(secret.value.toByteArray(Charsets.UTF_8), body)
              .toHexString(HexFormat.Default)

  @Nested
  inner class SharedSecret {
    @Test
    fun `should validate secret using sha-256`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val body = "Hello, World!"
      val request =
          Request(Method.POST, "/webhook")
              .body(body)
              .header("X-Hub-Signature-256", sign(body, secret))

      val response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was not invoked").isTrue()
      assertThat(response.status).isEqualTo(Status.OK)
    }

    @Test
    fun `should stop execution of request with invalid signature`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val headers: Headers =
          listOf(
              "X-Hub-Signature-256" to
                  "sha256=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
          )
      val request = Request(Method.POST, "/webhook").body("Hello, World!").replaceHeaders(headers)

      val response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
      assertThat(response.status).isEqualTo(Status.UNAUTHORIZED)
    }

    @Test
    fun `should stop execution of request with missing signature`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val request = Request(Method.POST, "/webhook").body("Hello, World!")

      val response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
      assertThat(response.status).isEqualTo(Status.UNAUTHORIZED)
    }
  }

  @Nested
  inner class PerClientSecret {
    private val clientId = "test-org"
    private val clientSecret = WebhookOptions.Secret("client-specific-secret")
    private val clientSecrets = mapOf(clientId to clientSecret)

    private fun resolverFromMap(
        secrets: Map<String, WebhookOptions.Secret>
    ): (Request) -> WebhookOptions.Secret? = { request ->
      // In production, the clientId is extracted via a Path lens.
      // In these unit tests we pass it as a query parameter for simplicity.
      val id = request.query("clientId")
      id?.let { secrets[it] }
    }

    @Test
    fun `should validate with correct per-client secret`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val body = "Hello, World!"
      val request =
          Request(Method.POST, "/webhook?clientId=$clientId")
              .body(body)
              .header("X-Hub-Signature-256", sign(body, clientSecret))

      val response =
          WebhookSecretValidatorFilter(resolverFromMap(clientSecrets))
              .invoke(okHandler)
              .invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was not invoked").isTrue()
      assertThat(response.status).isEqualTo(Status.OK)
    }

    @Test
    fun `should reject request signed with wrong client secret`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val wrongSecret = WebhookOptions.Secret("wrong-secret")
      val body = "Hello, World!"
      val request =
          Request(Method.POST, "/webhook?clientId=$clientId")
              .body(body)
              .header("X-Hub-Signature-256", sign(body, wrongSecret))

      val response =
          WebhookSecretValidatorFilter(resolverFromMap(clientSecrets))
              .invoke(okHandler)
              .invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
      assertThat(response.status).isEqualTo(Status.UNAUTHORIZED)
    }

    @Test
    fun `should return 404 for unknown client id`() {
      val (nextHandlerWasCalled, okHandler) = nextHandlerSpy()
      val body = "Hello, World!"
      val unknownClientId = "unknown-org"
      val request =
          Request(Method.POST, "/webhook?clientId=$unknownClientId")
              .body(body)
              .header("X-Hub-Signature-256", sign(body, clientSecret))

      val response =
          WebhookSecretValidatorFilter(resolverFromMap(clientSecrets))
              .invoke(okHandler)
              .invoke(request)

      assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
      assertThat(response.status).isEqualTo(Status.NOT_FOUND)
    }
  }
}
