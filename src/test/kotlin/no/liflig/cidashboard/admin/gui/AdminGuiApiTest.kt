package no.liflig.cidashboard.admin.gui

import io.restassured.RestAssured
import org.http4k.core.Status
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.Integration

@Integration
class AdminGuiApiTest {

  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension()
  }

  @BeforeEach
  fun setUp() {
    RestAssured.port = infra.app.config.apiOptions.serverPort.value
  }

  @Test
  fun `admin index should redirect to ci-statuses when cognito bypass enabled`() {
    RestAssured.given()
        .redirects()
        .follow(false)
        .`when`()
        .get("/admin")
        .then()
        .assertThat()
        .statusCode(Status.FOUND.code)
        .header("Location", "/admin/ci-statuses")
  }

  @Test
  fun `ci-statuses page should return 200 when cognito bypass enabled`() {
    RestAssured.`when`()
        .get("/admin/ci-statuses")
        .then()
        .assertThat()
        .statusCode(Status.OK.code)
        .contentType("text/html")
  }

  @Test
  fun `integration guide page should return 200 when cognito bypass enabled`() {
    RestAssured.`when`()
        .get("/admin/integration")
        .then()
        .assertThat()
        .statusCode(Status.OK.code)
        .contentType("text/html")
  }

  @Test
  fun `configs page should return 200 when cognito bypass enabled`() {
    RestAssured.`when`()
        .get("/admin/configs")
        .then()
        .assertThat()
        .statusCode(Status.OK.code)
        .contentType("text/html")
  }
}
