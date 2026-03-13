package no.liflig.cidashboard.admin.gui

import io.restassured.RestAssured
import org.http4k.core.Status
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.Integration

@Integration
class AdminGuiOAuthApiTest {

  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension(cognitoBypassEnabled = false)
  }

  @BeforeEach
  fun setUp() {
    RestAssured.port = infra.app.config.apiOptions.serverPort.value
  }

  @Test
  fun `admin index should redirect to oauth provider when not authenticated`() {
    RestAssured.given()
        .redirects()
        .follow(false)
        .`when`()
        .get("/admin")
        .then()
        .assertThat()
        .statusCode(Status.TEMPORARY_REDIRECT.code)
        .header("Location", org.hamcrest.Matchers.containsString("oauth2/authorize"))
  }

  @Test
  fun `admin ci-statuses should redirect to oauth provider when not authenticated`() {
    RestAssured.given()
        .redirects()
        .follow(false)
        .`when`()
        .get("/admin/ci-statuses")
        .then()
        .assertThat()
        .statusCode(Status.TEMPORARY_REDIRECT.code)
        .header("Location", org.hamcrest.Matchers.containsString("oauth2/authorize"))
  }

  @Test
  fun `oauth callback endpoint should exist and handle missing code`() {
    RestAssured.`when`()
        .get("/admin/oauth/callback")
        .then()
        .assertThat()
        .statusCode(
            org.hamcrest.Matchers.anyOf(
                org.hamcrest.Matchers.equalTo(Status.BAD_REQUEST.code),
                org.hamcrest.Matchers.equalTo(Status.FORBIDDEN.code),
                org.hamcrest.Matchers.equalTo(Status.TEMPORARY_REDIRECT.code),
            )
        )
  }

  @Test
  fun `oauth callback should set csrf cookie on redirect`() {
    RestAssured.given()
        .redirects()
        .follow(false)
        .`when`()
        .get("/admin/ci-statuses")
        .then()
        .assertThat()
        .cookie("cognitoCsrf", org.hamcrest.Matchers.notNullValue())
  }
}
