package no.liflig.cidashboard.admin.gui

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import no.liflig.cidashboard.dashboard.UseCiStatusRepo
import no.liflig.cidashboard.dashboard.UseDashboardConfigRepo

class AdminGuiService(
    private val ciStatusRepo: UseCiStatusRepo<List<CiStatusRow>>,
    private val configRepo: UseDashboardConfigRepo<List<ConfigRow>>,
) {
  private val dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

  fun getCiStatuses(): List<CiStatusRow> {
    return ciStatusRepo { repo ->
      repo.getAll().map { status ->
        CiStatusRow(
            id = status.id.value,
            repoOwner = status.repo.owner.value,
            repoName = status.repo.name.value,
            branch = status.branch.value,
            status = status.lastStatus.name,
            lastUpdated = dateFormatter.format(status.lastUpdatedAt),
        )
      }
    }
  }

  fun getConfigs(): List<ConfigRow> {
    return configRepo { repo ->
      repo.getAll().map { config ->
        ConfigRow(
            id = config.id.value,
            displayName = config.displayName,
            orgMatchers = config.orgMatchers.joinToString(", ") { it.matcher.pattern },
        )
      }
    }
  }
}
