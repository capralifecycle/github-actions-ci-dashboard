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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled("WIP")
class WebhookSecretValidatorFilterTest {

  val secret = WebhookOptions.Secret("It's a Secret to Everybody")

  @Test
  fun `should validate secret using sha-256`() {
    // Given
    val headers: Headers =
        listOf(
            "X-Hub-Signature-256" to
                "sha256=757107ea0eb2509fc211221cce984b8a37570b6d7586c22c46f4379c8b043e17")

    val nextHandlerWasCalled = AtomicBoolean(false)
    val okHandler: HttpHandler = { _ ->
      nextHandlerWasCalled.set(true)
      Response(Status.OK)
    }
    val request = Request(Method.POST, "/webhook").body("Hello, World!").replaceHeaders(headers)

    // When
    val response: Response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

    // Then
    assertThat(nextHandlerWasCalled).`as`("Next handler was not invoked").isTrue()
    assertThat(response.status).isEqualTo(Status.OK)
  }

  @Test
  fun `should stop execution of request with invalid signature`() {
    // Given
    val headers: Headers =
        listOf(
            "X-Hub-Signature-256" to
                "sha256=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")

    val nextHandlerWasCalled = AtomicBoolean(false)
    val okHandler: HttpHandler = { _ ->
      nextHandlerWasCalled.set(true)
      Response(Status.OK)
    }
    val request = Request(Method.POST, "/webhook").body("Hello, World!").replaceHeaders(headers)

    // When
    val response: Response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

    // Then
    assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
    assertThat(response.status).isEqualTo(Status.UNAUTHORIZED)
  }

  @Test
  fun `should stop execution of request with missing signature`() {
    // Given
    val headers: Headers = emptyList()

    val nextHandlerWasCalled = AtomicBoolean(false)
    val okHandler: HttpHandler = { _ ->
      nextHandlerWasCalled.set(true)
      Response(Status.OK)
    }
    val request = Request(Method.POST, "/webhook").body("Hello, World!").replaceHeaders(headers)

    // When
    val response: Response = WebhookSecretValidatorFilter(secret).invoke(okHandler).invoke(request)

    // Then
    assertThat(nextHandlerWasCalled).`as`("Next handler was invoked").isFalse()
    assertThat(response.status).isEqualTo(Status.UNAUTHORIZED)
  }
}
