package no.liflig.cidashboard.persistence

import java.sql.ResultSet
import kotlin.jvm.optionals.getOrNull
import mu.KotlinLogging
import mu.withLoggingContext
import org.jdbi.v3.core.Handle

/** Reads and writes [CiStatus] to a database. */
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
            .bind("id", status.id.value)
            .bind("data", status.toJson())
            .execute()

    if (updatedCount != 1) {
      log.warn {
        "Update of status ${status.id} (${status.repo.owner.value}/${status.repo.name.value}/${status.branch.value}) " +
            "did not result in the expected number of changed rows 1, but instead: $updatedCount"
      }
    } else {
      withLoggingContext(
          "status.lastUpdatedAt" to status.lastUpdatedAt.toString(),
          "status.id" to status.id.value) {
            log.debug {
              "Saved status id ${status.id} to table $TABLE_NAME with state ${status.lastStatus.name}"
            }
          }
    }
  }

  /** @return list of all statuses, sorted by [CiStatus.lastUpdatedAt] descending. */
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

  fun getById(id: CiStatusId): CiStatus? {
    return databaseHandle
        .select("SELECT data FROM $TABLE_NAME WHERE id = :id")
        .bind("id", id.value)
        .map { rs: ResultSet, _ -> CiStatus.fromJson(rs.getString("data")) }
        .findFirst()
        .getOrNull()
        .also { log.debug { "Fetched ${it?.id} from $TABLE_NAME" } }
  }

  fun deleteAll() {
    databaseHandle.createUpdate("TRUNCATE TABLE $TABLE_NAME").execute()
  }
}
