package no.liflig.cidashboard.dashboard

import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.jdbi.v3.core.Jdbi

class DashboardUpdatesService(private val withHandle: DatabaseHandle<List<CiStatus>>) {

  fun handleDashboardUpdate(dashboardId: String?): List<CiStatus> {
    return withHandle { repo -> repo.getAll() }
  }
}

fun interface DatabaseHandle<T> {
  operator fun invoke(block: (CiStatusRepo) -> T): T
}

class JdbiDatabaseHandle(private val jdbi: Jdbi) : DatabaseHandle<List<CiStatus>> {
  override fun invoke(block: (CiStatusRepo) -> List<CiStatus>): List<CiStatus> {
    return jdbi.withHandle<List<CiStatus>, Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}
