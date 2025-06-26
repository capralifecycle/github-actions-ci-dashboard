package no.liflig.cidashboard.persistence

import java.sql.ResultSet
import kotlin.jvm.optionals.getOrNull
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.logging.getLogger
import org.jdbi.v3.core.Handle

/** Reads and writes [CiStatus] to a database. */
class DashboardConfigRepo(private val databaseHandle: Handle) {

  companion object {
    const val TABLE_NAME = "dashboard_config"

    private val log = getLogger()
  }

  fun save(config: DashboardConfig) {
    val updatedCount =
        databaseHandle
            .createUpdate(
                """INSERT INTO $TABLE_NAME (id, data)
                  |     VALUES (:id, :data::jsonb)
                  |ON CONFLICT (id) DO UPDATE
                  |        SET data = :data::jsonb"""
                    .trimMargin(),
            )
            .bind("id", config.id.value)
            .bind("data", config.toJson())
            .execute()

    if (updatedCount != 1) {
      log.warn {
        "Update of config ${config.id} did not result in the expected number of changed rows 1, " +
            "but instead: $updatedCount"
      }
    } else {
      log.debug {
        field("config.id", config.id.value)
        "Saved status id ${config.id} to table $TABLE_NAME"
      }
    }
  }

  fun getById(id: DashboardConfigId): DashboardConfig? {
    return databaseHandle
        .select("SELECT data FROM $TABLE_NAME WHERE id = :id")
        .bind("id", id.value)
        .map { rs: ResultSet, _ -> DashboardConfig.fromJson(rs.getString("data")) }
        .findFirst()
        .getOrNull()
        .also { log.debug { "Fetched ${it?.id} from ${TABLE_NAME}" } }
  }
}
