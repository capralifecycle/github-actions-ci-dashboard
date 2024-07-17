package no.liflig.cidashboard.dashboard

import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.jdbi.v3.core.Jdbi

class DashboardUpdatesService(private val withHandle: DatabaseHandle<DashboardData>) {

  fun getUpdatedDashboardData(dashboardId: String?): DashboardData {
    // TODO use dashboardId to get settings and filters repos etc.
    return withHandle { repo ->
      val all = repo.getAll()

      val maxStatusesToReturn = 20
      DashboardData(
          lastBuilds = all.take(maxStatusesToReturn),
          allFailedBuilds = all.filter { it.lastStatus == CiStatus.PipelineStatus.FAILED })
    }
  }
}

data class DashboardData(val lastBuilds: List<CiStatus>, val allFailedBuilds: List<CiStatus>)

fun interface DatabaseHandle<T> {
  operator fun invoke(block: (CiStatusRepo) -> T): T
}

class JdbiDatabaseHandle(private val jdbi: Jdbi) : DatabaseHandle<DashboardData> {
  override fun invoke(block: (CiStatusRepo) -> DashboardData): DashboardData {
    return jdbi.withHandle<DashboardData, Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}
