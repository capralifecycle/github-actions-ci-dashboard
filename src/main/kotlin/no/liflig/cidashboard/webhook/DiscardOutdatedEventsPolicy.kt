package no.liflig.cidashboard.webhook

import no.liflig.cidashboard.persistence.CiStatus

/**
 * If webhook events are sent in the wrong order, with a retry or after a delay, we don't want to
 * overwrite the newer persisted data.
 */
object DiscardOutdatedEventsPolicy {
  fun shouldDiscard(existing: CiStatus?, incomingEvent: CiStatus): Boolean {
    return existing != null && incomingEvent.lastUpdatedAt.isBefore(existing.lastUpdatedAt)
  }
}
