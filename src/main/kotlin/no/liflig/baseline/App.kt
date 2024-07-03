package no.liflig.baseline

import mu.KotlinLogging
import net.logstash.logback.marker.Markers
import no.liflig.baseline.api.ApiServer
import no.liflig.baseline.api.ApiServices
import no.liflig.baseline.api.HealthService
import no.liflig.baseline.common.config.Config
import no.liflig.baseline.common.database.DatabaseConfigurator
import no.liflig.baseline.common.database.DbPassword
import no.liflig.baseline.common.database.DbUrl
import no.liflig.baseline.common.database.DbUsername
import no.liflig.baseline.common.observability.OpenTelemetryConfig
import no.liflig.baseline.examplefeature.MyOtherService
import no.liflig.baseline.examplefeature.MyService
import org.jdbi.v3.core.Jdbi

/**
 * The main entry point for the application. Should be started from [Main].
 *
 * @param config any options like ports and urls should be set here. Override values in the config
 *   when doing testing of [App].
 */
class App(private val config: Config) {
  private val logger = KotlinLogging.logger {}
  private val runningTasks = mutableListOf<AutoCloseable>()
  private val jdbi: Jdbi = createJdbiInstance(config)

  fun start() {
    OpenTelemetryConfig().configure()
    logger.info(Markers.append("buildInfo", config.buildInfo.toJson())) { "Starting application" }

    startApi(jdbi)
  }

  fun stop() {
    runningTasks.forEach { it.close() }
    runningTasks.clear()
  }

  private fun startApi(jdbi: Jdbi) {
    val apiOptions = config.apiOptions

    val healthService = HealthService(apiOptions.applicationName, config.buildInfo)
    val myService = MyService(jdbi)
    val myOtherService = MyOtherService(jdbi)
    val services = ApiServices(healthService, myService, myOtherService)

    val server = ApiServer(apiOptions, services).create()
    server.start()
    runningTasks.add(server)
    logger.info { "Server started on http://0.0.0.0:${apiOptions.serverPort}" }
  }
}

private fun createJdbiInstance(config: Config): Jdbi =
    config.database.run {
      DatabaseConfigurator.createJdbiInstance(
          DbUrl(jdbcUrl),
          DbUsername(username),
          DbPassword(password),
      )
    }
