package no.liflig.cidashboard.webhook

import mu.KotlinLogging
import mu.withLoggingContext
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.jdbi.v3.core.Jdbi

/**
 * Handles events and decides what to do with them, like storing to a repo or not.
 *
 * CALS-821
 */
class IncomingWebhookService(private val inTransaction: Transaction) {

  companion object {
    private val log = KotlinLogging.logger {}
  }

  fun handlePing(ping: GitHubWebhookPing) {
    withLoggingContext("webhook.event" to ping.toString()) { log.info { "Got ping from GitHub" } }
  }

  fun handleWorkflowRun(workflowRun: GitHubWebhookWorkflowRun) {
    inTransaction { repo ->
      val existing: CiStatus? = repo.getById(workflowRun.workflow.id)
      val incoming = workflowRun.toCiStatus()
      if (existing == null || existing.lastUpdatedAt.isBefore(incoming.lastUpdatedAt)) {
        repo.save(incoming)
      }
    }
  }
}

fun interface Transaction {
  operator fun invoke(block: (CiStatusRepo) -> Unit)
}

class JdbiTransaction(private val jdbi: Jdbi) : Transaction {
  override fun invoke(block: (CiStatusRepo) -> Unit) {
    jdbi.useTransaction<Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}
