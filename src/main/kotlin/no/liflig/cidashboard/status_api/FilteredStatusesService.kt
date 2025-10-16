package no.liflig.cidashboard.status_api

import no.liflig.cidashboard.dashboard.UseCiStatusRepo
import no.liflig.cidashboard.persistence.CiStatus

class FilteredStatusesService(private val withCiStatusRepo: UseCiStatusRepo<List<CiStatus>>) {

  fun getFilteredCiStatuses(
      repoFilter: List<Regex>,
      userFilter: List<String>,
      count: Int?,
      includeAllFailures: Boolean,
  ): List<CiStatus> {

    return withCiStatusRepo { repo ->
      val filtered =
          repo
              .getAll()
              .filter { status ->
                if (repoFilter.isEmpty()) {
                  true
                } else {
                  repoFilter.any { filter -> filter.matches(status.repo.name.value) }
                }
              }
              .filter { status ->
                if (userFilter.isEmpty()) {
                  true
                } else {
                  userFilter.contains(status.triggeredBy.value) ||
                      userFilter.contains(status.lastCommit.commiter.username.value)
                }
              }

      var result = filtered.toMutableList()
      if (count != null) {
        require(count > 0) { "Invalid value for count. Must be positive" }

        result = filtered.take(count).toMutableList()
      }

      if (includeAllFailures) {

        val failed = filtered.filter { it.lastStatus == CiStatus.PipelineStatus.FAILED }

        failed.forEach { fail ->
          if (!result.contains(fail)) {
            result.add(fail)
          }
        }
      }

      result
    }
  }
}
