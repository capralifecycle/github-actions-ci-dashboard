package test.util

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import java.net.ServerSocket
import mu.KotlinLogging
import no.liflig.cidashboard.App
import no.liflig.cidashboard.common.config.Config
import no.liflig.cidashboard.common.config.DbConfig
import no.liflig.cidashboard.common.config.Port
import no.liflig.cidashboard.common.config.WebhookOptions
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.testcontainers.containers.PostgreSQLContainer

/**
 * A Junit5 Extension. Responsible for starting infrastructure (database etc) and the [App] itself.
 *
 * An "Acceptance Test" is a high-level, feature oriented test to verify that the system does what
 * it is supposed to. [Read about it here](http://www.growing-object-oriented-software.com)
 *
 * To test at this high level and end-to-end, the system requires infrastructure or mocks/simulators
 * to properly interact with external systems.
 */
class AcceptanceTestExtension : Extension, BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

  lateinit var app: App

  val gitHub = GitHub()
  val database = Database()
  val tvBrowser = TvBrowser()

  override fun beforeAll(context: ExtensionContext) {
    database.start()

    val config = Config.load().let { database.applyTo(it) }.let { setUnusedHttpPort(it) }

    app = App(config)
    app.start()

    gitHub.initialize(
        port = config.apiOptions.serverPort,
        webhookPath = config.webhookOptions.path,
        webhookSecret = config.webhookOptions.secret)

    tvBrowser.initialize(config.apiOptions.serverPort)
  }

  override fun afterAll(context: ExtensionContext) {
    database.stop()
  }

  override fun beforeEach(context: ExtensionContext) {
    database.clearAllData()
  }

  private fun setUnusedHttpPort(config: Config): Config {
    val RANDOM_PORT = 0
    val port = ServerSocket(RANDOM_PORT).use { it.localPort }

    return config.copy(apiOptions = config.apiOptions.copy(serverPort = Port(port)))
  }

  class GitHub {
    private val log = KotlinLogging.logger {}

    private var webhookDestinationPort: Port = Port(8080)
    private lateinit var webhookPostRequest: RequestSpecification

    fun initialize(port: Port, webhookPath: String, webhookSecret: WebhookOptions.Secret) {
      this.webhookDestinationPort = port

      webhookPostRequest =
          RequestSpecBuilder()
              .setPort(webhookDestinationPort.value)
              .setContentType(ContentType.JSON)
              .build()
    }

    fun sendWebhook(payload: WebhookPayload) {
      log.debug { "Sending webhook $payload ..." }

      RestAssured.given(webhookPostRequest)
          .body(payload.asJson())
          .log()
          .all()
          .post()
          .then()
          .statusCode(200)
          .log()
          .ifError()
    }
  }

  class Database {
    private val postgresContainer: PostgreSQLContainer<*> =
        PostgreSQLContainer(extractPostgresImageFromDockerCompose())
            .withDatabaseName("app")
            .withUsername("user")
            .withPassword("password")
            // Enables query logs in the container:
            .withCommand("postgres", "-c", "fsync=off", "-c", "log_statement=all")

    private lateinit var jdbi: Jdbi

    fun start() {
      postgresContainer.start()
      jdbi = with(postgresContainer) { Jdbi.create(jdbcUrl, username, password) }
    }

    fun stop() {
      postgresContainer.stop()
    }

    fun applyTo(config: Config): Config =
        config.copy(
            database =
                with(postgresContainer) {
                  DbConfig(
                      jdbcUrl = jdbcUrl,
                      username = username,
                      password = password,
                      dbname = databaseName,
                      hostname = host,
                      port = firstMappedPort)
                })

    /** Does not clear Flyway migrations table or delete tables. This only deletes rows */
    fun clearAllData() {
      jdbi.useHandle<Exception> { handle ->
        handle.execute(
            """DO ${'$'}${'$'}DECLARE tablename TEXT;
BEGIN
    FOR tablename IN (SELECT table_name
                       FROM information_schema.tables
                       WHERE table_schema = 'public'
                         AND table_type = 'BASE TABLE'
                         AND table_catalog = 'app'
                         AND table_name not like 'flyway%')
    LOOP
        EXECUTE 'TRUNCATE TABLE ' || tablename || ' RESTART IDENTITY CASCADE;';
    END LOOP;
END${'$'}${'$'};""")
      }
    }
  }

  class TvBrowser {
    private var dashboardServerPort: Port = Port(8080)

    fun initialize(port: Port) {
      this.dashboardServerPort = port
    }

    fun navigateToDashboard() {}

    fun verifyDashboardIsEmpty() {}

    fun verifyDashboardHasRepoInProgress(repoName: String) {}

    fun verifyDashboardHasRepoInSuccess(repoName: String) {}
  }
}

data class WebhookPayload(private val repoName: String = "example-repo") {
  object ExampleRepo {
    const val repoName: String = "example-repo"

    val WORKFLOW_RUN_1_START = WebhookPayload(repoName)
    val WORKFLOW_RUN_1_SUCCESS = WebhookPayload(repoName)
  }

  fun asJson(): String {
    return "{}"
  }
}
