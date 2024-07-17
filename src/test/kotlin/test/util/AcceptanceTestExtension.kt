package test.util

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.assertions.PlaywrightAssertions
import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.specification.RequestSpecification
import java.net.ServerSocket
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import mu.KotlinLogging
import no.liflig.cidashboard.App
import no.liflig.cidashboard.common.config.Config
import no.liflig.cidashboard.common.config.DbConfig
import no.liflig.cidashboard.common.config.Port
import no.liflig.cidashboard.common.config.WebhookOptions
import no.liflig.cidashboard.persistence.CiStatus
import org.http4k.security.HmacSha256
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
class AcceptanceTestExtension(val fastPoll: Boolean = true) :
    Extension, BeforeAllCallback, AfterAllCallback, BeforeEachCallback {

  lateinit var app: App

  val gitHub = GitHub()
  val database = Database()
  val tvBrowser = TvBrowser()

  override fun beforeAll(context: ExtensionContext) {
    database.start()

    val config =
        Config.load()
            .let { database.applyTo(it) }
            .let { setUnusedHttpPort(it) }
            .let {
              if (fastPoll) {
                setFastPollingInterval(it)
              } else {
                it
              }
            }

    app = App(config)
    app.start()

    gitHub.initialize(
        port = config.apiOptions.serverPort,
        webhookPath = config.webhookOptions.path,
        webhookSecret = config.webhookOptions.secret)

    tvBrowser.initialize(
        port = config.apiOptions.serverPort,
        authToken = "todo-add-token-via-config-api",
        dashboardId = "abc")
  }

  override fun afterAll(context: ExtensionContext) {
    app.stop()
    database.stop()
    tvBrowser.close()
  }

  override fun beforeEach(context: ExtensionContext) {
    database.clearAllData()
  }

  private fun setUnusedHttpPort(config: Config): Config {
    val RANDOM_PORT = 0
    val port = ServerSocket(RANDOM_PORT).use { it.localPort }

    return config.copy(apiOptions = config.apiOptions.copy(serverPort = Port(port)))
  }

  /** Make tests faster, by polling more rapidly so Playwright waits less. */
  private fun setFastPollingInterval(config: Config): Config {
    return config.copy(apiOptions = config.apiOptions.copy(updatesPollRate = 500.milliseconds))
  }

  class GitHub {
    private val log = KotlinLogging.logger {}

    private var webhookDestinationPort: Port = Port(8080)
    private lateinit var webhookPath: String
    private var webhookSecret: WebhookOptions.Secret = WebhookOptions.Secret("unknown")

    private lateinit var webhookPostRequest: RequestSpecification

    fun initialize(port: Port, webhookPath: String, webhookSecret: WebhookOptions.Secret) {
      this.webhookDestinationPort = port
      this.webhookPath = webhookPath
      this.webhookSecret = webhookSecret

      webhookPostRequest =
          RequestSpecBuilder()
              .setPort(webhookDestinationPort.value)
              .setContentType(ContentType.JSON)
              .build()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun sign(body: String): Header {
      return Header(
          "X-Hub-Signature-256",
          "sha256=" +
              HmacSha256.hmacSHA256(webhookSecret.value.toByteArray(Charsets.UTF_8), body)
                  .toHexString(HexFormat.Default))
    }

    fun sendWebhook(payload: WebhookPayload) {
      log.info { "Sending webhook $payload ..." }

      val body = payload.asJson()

      RestAssured.given(webhookPostRequest)
          .body(body)
          .header(sign(body))
          .header("X-GitHub-Event", payload.type)
          .log()
          .method()
          .log()
          .uri()
          .log()
          .headers()
          .post(webhookPath)
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

  class TvBrowser : AutoCloseable {
    private val log = KotlinLogging.logger {}

    // Failed Playwright Traces can be viewed in https://trace.playwright.dev/
    private val playwright: Playwright = Playwright.create()
    private val browser: Browser =
        playwright
            .chromium()
            .launch(
                // Uncomment for manual testing:
                /*BrowserType.LaunchOptions().setHeadless(false).setSlowMo(1000.0)*/ )
    private val context: BrowserContext =
        browser.newContext(
            Browser.NewContextOptions()
                .setLocale("no-nb")
                .setTimezoneId("Europe/Oslo")
                .setScreenSize(1920, 1080)
                .setViewportSize(1920, 1080))

    val page: Page = context.newPage()

    private var dashboardServerPort: Port = Port(8080)
    private lateinit var authToken: String
    private lateinit var dashboardId: String

    fun initialize(port: Port, authToken: String, dashboardId: String) {
      this.dashboardServerPort = port
      this.authToken = authToken
      this.dashboardId = dashboardId

      PlaywrightAssertions.setDefaultAssertionTimeout(10.seconds.inWholeMilliseconds.toDouble())
    }

    override fun close() {
      playwright.close()
    }

    private fun path(path: String): String =
        "http://localhost:${dashboardServerPort.value}/$path?token=${authToken}&id=${dashboardId}"

    fun navigateToDashboard() {
      log.info { "Navigating to dashboard /index.html" }
      page.navigate(path("index.html"))
      PlaywrightAssertions.assertThat(page).hasTitle("CI Dashboard")
    }

    fun verifyDashboardIsEmpty() {
      PlaywrightAssertions.assertThat(page).hasURL(path("index.html"))
      PlaywrightAssertions.assertThat(page.locator("#first-load")).not().isVisible()

      PlaywrightAssertions.assertThat(page.locator("#statuses")).isVisible()
      PlaywrightAssertions.assertThat(page.locator("#statuses > .status")).hasCount(0)
    }

    fun verifyDashboardHasRepoInStatus(repoName: String, status: CiStatus.PipelineStatus) {
      val ciStatus = page.locator(".status--$status")
      PlaywrightAssertions.assertThat(ciStatus).isVisible()
      PlaywrightAssertions.assertThat(ciStatus.locator(".status__repo-name")).hasText(repoName)
    }

    fun verifyAllFailedBuildsIsListingRepo(repoName: String) {
      PlaywrightAssertions.assertThat(page.locator("#failed-builds")).containsText(repoName)
    }
  }
}

interface WebhookPayload {
  /** `"workflow_run"`, `"ping"` etc. */
  val type: String

  fun asJson(): String
}

data class FileWebhookPayload(
    val name: String,
    private val filePath: String,
    override val type: String
) : WebhookPayload {
  init {
    require(!filePath.startsWith("/")) { "Path should not start with '/'" }
  }

  object Ping {
    val WEBHOOK_CREATED_PING =
        FileWebhookPayload(
            "WEBHOOK_CREATED_PING", "acceptancetests/webhook/github-ping-body.json", "ping")
  }

  object ExampleRepo {
    const val repoName: String = "github-actions-ci-dashboard"

    val WORKFLOW_RUN_1_REQUESTED =
        FileWebhookPayload(
            "WORKFLOW_RUN_1_REQUESTED",
            "acceptancetests/webhook/user-workflow_run-requested.json",
            "workflow_run")
    val WORKFLOW_RUN_1_IN_PROGRESS =
        FileWebhookPayload(
            "WORKFLOW_RUN_1_IN_PROGRESS",
            "acceptancetests/webhook/user-workflow_run-in_progress.json",
            "workflow_run")
    val WORKFLOW_RUN_1_COMPLETED_FAILURE =
        FileWebhookPayload(
            "WORKFLOW_RUN_1_FAILURE",
            "acceptancetests/webhook/user-workflow_run-completed-failure.json",
            "workflow_run")
    val WORKFLOW_RUN_1_COMPLETED_SUCCESS =
        FileWebhookPayload(
            "WORKFLOW_RUN_1_SUCCESS",
            "acceptancetests/webhook/renovate-bot-workflow_run-completed-success.json",
            "workflow_run")
  }

  override fun asJson(): String {
    return loadResource(filePath)
  }
}
