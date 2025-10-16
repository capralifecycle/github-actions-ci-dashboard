package no.liflig.cidashboard.dashboard

import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.admin.config.applyFilter
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.cidashboard.persistence.DashboardConfigRepo
import org.jdbi.v3.core.Jdbi

class DashboardUpdatesService(
    private val useCiStatusRepo: UseCiStatusRepo<DashboardData>,
    private val useDashboardConfigRepo: UseDashboardConfigRepo<DashboardConfig?>,
) {

  fun getUpdatedDashboardData(dashboardConfigId: DashboardConfigId): DashboardData {

    val config = dashboardConfigId.let { useDashboardConfigRepo { repo -> repo.getById(it) } }

    return useCiStatusRepo { repo ->
      val all = repo.getAll()

      val filtered = config?.applyFilter(all) ?: all

      val maxStatusesToReturn = 20
      DashboardData(
          lastBuilds = filtered.take(maxStatusesToReturn),
          allFailedBuilds = filtered.filter { it.lastStatus == CiStatus.PipelineStatus.FAILED },
          config = config,
      )
    }
  }
}

data class DashboardData(
    val lastBuilds: List<CiStatus>,
    val allFailedBuilds: List<CiStatus>,
    val config: DashboardConfig? = null,
)

/** Makes testing of Services easier, because mocking JDBI directly is a hassle. */
fun interface UseCiStatusRepo<T> {
  operator fun invoke(block: (CiStatusRepo) -> T): T
}

class JdbiCiStatusDatabaseHandle<T>(private val jdbi: Jdbi) : UseCiStatusRepo<T> {
  override fun invoke(block: (CiStatusRepo) -> T): T {
    return jdbi.withHandle<T, Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}

fun interface UseDashboardConfigRepo<T> {
  operator fun invoke(block: (DashboardConfigRepo) -> T): T
}

class JdbiDashboardConfigDatabaseHandle<T>(private val jdbi: Jdbi) : UseDashboardConfigRepo<T> {
  override fun invoke(block: (DashboardConfigRepo) -> T): T {
    return jdbi.withHandle<T, Exception> { handle -> block(DashboardConfigRepo(handle)) }
  }
}
