package no.liflig.cidashboard.api

import io.restassured.RestAssured
import org.http4k.core.Status
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.Integration
import test.util.IntegrationTest

@Integration
class HealthApiTest {

  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension()
  }

  @BeforeEach
  fun setUp() {
    RestAssured.port = infra.app.config.apiOptions.serverPort.value
  }

  @IntegrationTest
  internal fun `health should respond 200 ok`() {
    RestAssured.`when`()
        .get("/health")
        .then()
        .assertThat()
        .statusCode(Status.OK.code)
        .log()
        .ifError()
  }
}
