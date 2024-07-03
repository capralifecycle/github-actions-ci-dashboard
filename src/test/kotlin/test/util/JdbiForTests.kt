package test.util

import java.io.File
import mu.KotlinLogging
import no.liflig.cidashboard.common.config.Config
import no.liflig.cidashboard.common.config.DbConfig
import org.jdbi.v3.core.Jdbi
import org.testcontainers.containers.PostgreSQLContainer

private val logger = KotlinLogging.logger {}

/** Uses Testcontainers PostgreSQL instance. */
fun Config.Companion.loadForTests(): Config =
    load()
        .copy(
            database =
                postgres.run {
                  DbConfig(
                      jdbcUrl = jdbcUrl,
                      username = username,
                      password = password,
                      dbname = databaseName,
                      hostname = host,
                      port = firstMappedPort)
                })

/** Helper function for clearing table content e.g. between tests. */
fun clearTable(tableName: String) {
  jdbiForTests.inTransaction<Int, Exception> { handle ->
    handle.createUpdate("TRUNCATE $tableName").execute()
  }
  logger.info { "Table [$tableName] cleaned" }
}

/**
 * Jdbi singleton instance used for all tests within this module so they all share the same
 * Testcontainers instance to improve performance.
 */
val jdbiForTests: Jdbi by lazy { postgres.run { Jdbi.create(jdbcUrl, username, password) } }

/** Create a Testcontainers PostgreSQL instance running in Docker. */
private val postgres by lazy {
  PostgreSQLContainer(extractPostgresImageFromDockerCompose()).apply { start() }
}

/** Use same postgres image for testing as we use when running app locally. */
private fun extractPostgresImageFromDockerCompose(): String {
  var dir = File(System.getProperty("user.dir"))
  var f: File

  // Traverse up to find docker-compose.yml.
  while (true) {
    f = dir.resolve("docker-compose.yml")
    if (f.exists()) break
    if (dir.parentFile == dir) throw IllegalStateException("Could not find docker-compose.yml")
    dir = dir.parentFile
  }

  return f.readText()
      .lines()
      .first { "image: postgres" in it }
      .substringAfter("image:")
      .trim()
      .also { logger.info { "Using postgres image [$it]" } }
}
