package no.liflig.cidashboard.api

import io.restassured.RestAssured
import no.liflig.cidashboard.App
import no.liflig.cidashboard.common.config.Config
import org.http4k.core.Status
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import test.util.IntegrationTest
import test.util.loadForTests

class HealthApiTest {

  private val config = Config.loadForTests()
  private val app = App(config)

  @BeforeEach
  fun setUp() {
    RestAssured.port = config.apiOptions.serverPort
    app.start()
  }

  @AfterEach
  fun tearDown() {
    app.stop()
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
