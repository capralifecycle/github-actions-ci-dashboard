package no.liflig.cidashboard.persistence

import java.sql.ResultSet
import mu.KotlinLogging
import mu.withLoggingContext
import org.jdbi.v3.core.Handle

/** Reads and writes CI statuses to a database. */
class CiStatusRepo(private val databaseHandle: Handle) {

  companion object {
    const val TABLE_NAME = "ci_status"

    private val log = KotlinLogging.logger {}
  }

  fun save(status: CiStatus) {
    val updatedCount =
        databaseHandle
            .createUpdate(
                """INSERT INTO $TABLE_NAME (id, data)
                  |     VALUES (:id, :data::jsonb)
                  |ON CONFLICT (id) DO UPDATE
                  |        SET data = :data::jsonb"""
                    .trimMargin())
            .bind("id", status.id)
            .bind("data", status.toJson())
            .execute()

    if (updatedCount != 1) {
      log.warn {
        "Update of status ${status.id} (${status.repo.owner.value}/${status.repo.name.value}/${status.branch.value}) " +
            "did not result in the expected number of changed rows 1, but instead: $updatedCount"
      }
    } else {
      withLoggingContext(
          "status.lastUpdatedAt" to status.lastUpdatedAt.toString(), "status.id" to status.id) {
            log.debug {
              "Saved status id ${status.id} to table $TABLE_NAME with state ${status.lastStatus.name}"
            }
          }
    }
  }

  fun getAll(): List<CiStatus> {
    return databaseHandle
        .select(
            "SELECT data FROM $TABLE_NAME ORDER BY (data ->> '${CiStatus::lastUpdatedAt.name}') DESC ")
        .map { rs: ResultSet, _ -> CiStatus.fromJson(rs.getString("data")) }
        .list()
        .also {
          withLoggingContext("repo.result.size" to it.size.toString()) {
            log.debug { "Fetched ${it.size} statuses from $TABLE_NAME" }
          }
        }
  }
}
