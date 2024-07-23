package no.liflig.cidashboard.dashboard

import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.jdbi.v3.core.Jdbi

class DashboardUpdatesService(private val useRepo: UseRepo<DashboardData>) {

  fun getUpdatedDashboardData(dashboardId: String?): DashboardData {
    // TODO use dashboardId to get settings and filters repos etc.
    return useRepo { repo ->
      val all = repo.getAll()

      val maxStatusesToReturn = 20
      DashboardData(
          lastBuilds = all.take(maxStatusesToReturn),
          allFailedBuilds = all.filter { it.lastStatus == CiStatus.PipelineStatus.FAILED })
    }
  }
}

data class DashboardData(val lastBuilds: List<CiStatus>, val allFailedBuilds: List<CiStatus>)

/** Makes testing of Services easier, because mocking JDBI directly is a hassle. */
fun interface UseRepo<T> {
  operator fun invoke(block: (CiStatusRepo) -> T): T
}

class JdbiDatabaseHandle<T>(private val jdbi: Jdbi) : UseRepo<T> {
  override fun invoke(block: (CiStatusRepo) -> T): T {
    return jdbi.withHandle<T, Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}
