package no.liflig.cidashboard.admin.auth

import no.liflig.cidashboard.common.config.CognitoConfig
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CognitoAuthServiceTest {

  private val testConfig =
      CognitoConfig(
          userPoolId = "eu-north-1_test",
          clientId = "test-client",
          clientSecret = "",
          domain = "test",
          region = "eu-north-1",
          requiredGroup = "admin",
          bypassEnabled = false,
          appBaseUrl = "http://localhost:8080",
      )

  @Test
  fun `should redirect to oauth provider when no token present`() {
    val authService = createService(testConfig)
    val filter = authService.authFilter()

    val request = Request(Method.GET, "/admin/ci-statuses")
    val response = filter { Response(Status.OK) }(request)

    assertThat(response.status).isEqualTo(Status.TEMPORARY_REDIRECT)
    assertThat(response.header("Location"))
        .contains("test.auth.eu-north-1.amazoncognito.com/oauth2/authorize")
  }

  @Test
  fun `should allow access when bypass is enabled`() {
    val configWithBypass = testConfig.copy(bypassEnabled = true)
    val authService = createService(configWithBypass)
    val filter = authService.authFilter()

    val request = Request(Method.GET, "/admin/ci-statuses")
    val response = filter { Response(Status.OK).body("success") }(request)

    assertThat(response.status).isEqualTo(Status.OK)
    assertThat(response.bodyString()).isEqualTo("success")
  }

  @Test
  fun `should add user to request context when bypass enabled`() {
    val configWithBypass = testConfig.copy(bypassEnabled = true)
    val authService = createService(configWithBypass)
    val filter = authService.authFilter()

    lateinit var capturedUser: CognitoUser
    val request = Request(Method.GET, "/admin/ci-statuses")
    filter {
      capturedUser = CognitoAuthService.requireCognitoUser(it)
      Response(Status.OK)
    }(request)

    assertThat(capturedUser.username).isEqualTo("bypass-user")
    assertThat(capturedUser.groups).contains("admin")
  }

  @Test
  fun `should require cognito user throw when no user present`() {
    val request = Request(Method.GET, "/test")

    val exception =
        assertThrows<IllegalStateException> { CognitoAuthService.requireCognitoUser(request) }

    assertThat(exception.message).contains("No Cognito user in request context")
  }

  @Test
  fun `should provide callback handler`() {
    val authService = createService(testConfig)

    assertThat(authService.callbackHandler()).isNotNull
  }

  private fun createService(config: CognitoConfig): CognitoAuthService {
    return CognitoAuthService(
        config = config,
        httpClient = { Response(Status.OK) },
    )
  }
}
