package no.liflig.cidashboard.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.TimeUnit
import javax.sql.DataSource
import kotlin.concurrent.thread
import no.liflig.cidashboard.common.database.DatabaseConfigurator.createJdbiInstance
import no.liflig.logging.getLogger
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.ConnectionFactory
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.transaction.SerializableTransactionRunner
import org.jdbi.v3.postgres.PostgresPlugin

@JvmInline value class DbUsername(val value: String)

@JvmInline value class DbUrl(val value: String)

@JvmInline value class DbPassword(val value: String)

/** Creates new instances of JDBI ready to use in your application with [createJdbiInstance]. */
object DatabaseConfigurator {
  private val log = getLogger()

  /**
   * Defined by `cpuCount * 2 + 2`.
   *
   * Anywhere between `cpuCount * 2` to `cpuCount * 4` may be a good spot.
   *
   * Note that postgres could cap out at 160 total connections, making you unable to deploy new
   * instances if you are scaling up. RDS scales it with the instance memory (RAM). Get the exact
   * value with `select * from pg_settings where name='max_connections';`
   */
  private const val CONNECTION_POOL_MAXIMUM_SIZE = 10

  /**
   * Creates a new JDBI instance with a Connection Pool, and automatically migrates the database.
   *
   * Flyway Database Migrations are read from `src/main/resources/migrations/`, and must be named
   * using the Flyway naming schema
   *
   * Call this once and store the result. Reuse this JDBI instance everywhere in your application.
   * Calling this method creates another connection pool and migration every time.
   */
  fun createJdbiInstance(
      /** The jdbc database url, like `"jdbc:postgresql://localhost:5432/myDatabase"`. */
      url: DbUrl,
      /** Database username, like `"app"`. */
      username: DbUsername,
      /** Database password, like `"hunter2"`. */
      password: DbPassword,
      /**
       * A human-readable name for the Connection pool. Shows up in metrics for pool usage. Useful
       * if you have several connection pools.
       */
      connectionPoolName: String = "default"
  ): Jdbi {
    val dataSource = createDataSource(url, username, password, connectionPoolName)
    val jdbi =
        Jdbi.create(PostgresConnectionFactory(dataSource))
            // The plugin configures mappings for some default types https://jdbi.org/#_postgresql
            .installPlugin(PostgresPlugin())

    // This handler will auto retry failures if you use
    // `jdbi.useTransaction<RuntimeException>(TransactionIsolationLevel.SERIALIZABLE) { handle ->
    // myCode } `.
    // Note that YOUR code inside `.inTransaction { myCode }` will be retried and called multiple
    // times!
    jdbi.transactionHandler = SerializableTransactionRunner()

    // Run database migrations
    migrate(dataSource)
    return jdbi
  }

  private fun createDataSource(
      url: DbUrl,
      username: DbUsername,
      password: DbPassword,
      name: String
  ): DataSource {
    val config =
        HikariConfig().apply {
          // The System property is for overriding the driver in tests, etc.
          driverClassName = System.getProperty("database.driver.class", "org.postgresql.Driver")
          jdbcUrl = url.value
          this.username = username.value
          this.password = password.value

          /*
           - If you are using AWS API-GW, you can set 30s here, as it hard-caps responses to 30s.
           - If the connection is used for SQS processing, set it close or below SQS Message Visibility Timeout.
           - If you can wait however much you like for a connection, put a high value like 10min.
          */
          connectionTimeout = TimeUnit.SECONDS.toMillis(60L)

          // You may want to lower this level if you allow database anomalies.
          // See Table 13.1 at https://www.postgresql.org/docs/current/transaction-iso.html
          transactionIsolation = IsolationLevel.TRANSACTION_SERIALIZABLE.name

          // Not very relevant if minimum == maximum.
          idleTimeout = TimeUnit.MINUTES.toMillis(10L)
          minimumIdle = CONNECTION_POOL_MAXIMUM_SIZE
          maximumPoolSize = CONNECTION_POOL_MAXIMUM_SIZE

          this.poolName = name
        }

    val dataSource = HikariDataSource(config)

    try {
      // Important! Otherwise, Postgres may keep the connections
      // even after this server has shut down
      Runtime.getRuntime()
          .addShutdownHook(
              thread(start = false, name = "hikari-shutdown") {
                if (!dataSource.isClosed) dataSource.close()
              })
    } catch (e: Exception) {
      log.error(e) { "Failed to add shutdown hook for hikari." }
    }

    return dataSource
  }

  private fun migrate(dataSource: DataSource) {
    val flyway =
        Flyway.configure()
            .baselineOnMigrate(true)
            .dataSource(dataSource)
            .locations("migrations")
            .load()
    /*
    Instead of SQL-file migrations, you can also extend BaseJavaMigration and define your migration as code.
    Read "Java-based migrations" at https://documentation.red-gate.com/fd/migrations-184127470.html#sql-based-migrations
    for more.
     */

    log.debug { "Running database migrations..." }
    flyway.migrate()
  }
}

/**
 * This does an extra step of executing `"DISCARD ALL"` before returning the connection to the pool.
 */
private class PostgresConnectionFactory(private val dataSource: DataSource) : ConnectionFactory {

  override fun openConnection(): Connection = dataSource.connection

  override fun closeConnection(conn: Connection) {
    postgresResetConnectionState(conn)
    conn.close()
  }

  private fun postgresResetConnectionState(conn: Connection) {
    try {
      conn.createStatement().use { statement ->
        // https://www.postgresql.org/docs/current/sql-discard.html
        statement.execute("DISCARD ALL;")
      }
    } catch (ignored: SQLException) {}
  }
}
