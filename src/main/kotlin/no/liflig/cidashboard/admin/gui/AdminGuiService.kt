package no.liflig.cidashboard.admin.gui

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.persistence.CiStatus

class AdminGuiService(
    private val ciStatusRepo: () -> List<CiStatus>,
    private val configRepo: () -> List<DashboardConfig>,
) {
  private val dateFormatter =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

  fun getCiStatuses(): List<CiStatusRow> {
    return ciStatusRepo().map { status ->
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

  fun getConfigs(): List<ConfigRow> {
    return configRepo().map { config ->
      ConfigRow(
          id = config.id.value,
          displayName = config.displayName,
          orgMatchers = config.orgMatchers.joinToString(", ") { it.matcher.pattern },
      )
    }
  }
}
