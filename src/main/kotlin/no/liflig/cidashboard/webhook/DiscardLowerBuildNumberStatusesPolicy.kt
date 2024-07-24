package no.liflig.cidashboard.webhook

import no.liflig.cidashboard.persistence.CiStatus

object DiscardLowerBuildNumberStatusesPolicy {
  fun shouldDiscard(existing: CiStatus?, incoming: CiStatus): Boolean {
    return (existing != null && existing.buildNumber > incoming.buildNumber)
  }
}
