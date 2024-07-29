package no.liflig.cidashboard.admin.config

import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.persistence.CiStatus

fun DashboardConfig.applyFilter(data: List<CiStatus>): List<CiStatus> {
  return data.filter { ciStatus ->
    // org filter
    this.orgMatchers.any { orgMatcher ->
      orgMatcher.matcher.matches(ciStatus.repo.owner.value) &&
          // repo filter
          orgMatcher.repoMatchers.any { repoMatcher ->
            repoMatcher.matcher.matches(ciStatus.repo.name.value) &&
                // repo branch filter
                repoMatcher.branchMatchers.any { branchMatcher ->
                  branchMatcher.matcher.matches(ciStatus.branch.value)
                }
          }
    }
  }
}
