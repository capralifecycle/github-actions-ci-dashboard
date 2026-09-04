package no.liflig.cidashboard.admin.config

import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.dashboard.UseDashboardConfigRepo
import no.liflig.cidashboard.persistence.DashboardConfigRepo
import org.jdbi.v3.core.Jdbi

class DashboardConfigService(
    private val inTransaction: DashboardConfigTransaction,
    private val useDashboardConfigRepo: UseDashboardConfigRepo<List<DashboardConfig>>,
) {

  fun handleListUpdate(configs: List<DashboardConfig>) {
    inTransaction { repo -> configs.forEach { config -> repo.save(config) } }
  }

  fun getAll(): List<DashboardConfig> = useDashboardConfigRepo { repo -> repo.getAll() }

  fun getById(id: DashboardConfigId): DashboardConfig? = getAll().find { it.id == id }
}

fun interface DashboardConfigTransaction {
  operator fun invoke(block: (DashboardConfigRepo) -> Unit)
}

class JdbiDashboardConfigTransaction(private val jdbi: Jdbi) : DashboardConfigTransaction {
  override fun invoke(block: (DashboardConfigRepo) -> Unit) {
    jdbi.useTransaction<Exception> { handle -> block(DashboardConfigRepo(handle)) }
  }
}
