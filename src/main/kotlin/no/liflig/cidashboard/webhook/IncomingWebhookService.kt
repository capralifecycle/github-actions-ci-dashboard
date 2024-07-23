package no.liflig.cidashboard.webhook

import mu.KotlinLogging
import mu.withLoggingContext
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.jdbi.v3.core.Jdbi

/**
 * Handles events and decides what to do with them, like storing to a repo or not.
 *
 * CALS-821
 */
class IncomingWebhookService(
    private val inTransaction: Transaction,
    /**
     * If non-empty, the events in [handleWorkflowRun] will only be persisted if the branch is
     * exactly contained in this list. It is case-sensitive.
     */
    private val branchWhitelist: BranchWhitelist = BranchWhitelist(emptyList()),
    private val workflowNameWhitelist: WorkflowNameWhitelist = WorkflowNameWhitelist(emptyList()),
) {

  companion object {
    private val log = KotlinLogging.logger {}
  }

  fun handlePing(ping: GitHubWebhookPing) {
    withLoggingContext("webhook.event" to ping.toString()) { log.info { "Got ping from GitHub" } }
  }

  fun handleWorkflowRun(workflowRun: GitHubWebhookWorkflowRun) {
    if (WhitelistBranchPolicy.shouldIgnoreEvent(workflowRun, branchWhitelist)) {
      log.debug { "Ignoring event from branch ${workflowRun.workflowRun.headBranch}" }
      return
    }

    if (WhitelistWorkflowNamePolicy.shouldIgnore(workflowRun, workflowNameWhitelist)) {
      log.debug { "Ignoring event from workflow named ${workflowRun.workflow.name}" }
      return
    }

    inTransaction { repo ->
      val existing: CiStatus? = repo.getById(CiStatusId.from(workflowRun))
      var incoming = workflowRun.toCiStatus()

      if (existing != null && incoming.lastUpdatedAt.isBefore(existing.lastUpdatedAt)) {
        // Outdated event
        return@inTransaction
      }

      if (incoming.lastStatus != CiStatus.PipelineStatus.SUCCEEDED) {
        // Keep old duration
        incoming = incoming.copy(durationOfLastSuccess = existing?.durationOfLastSuccess)
      }

      repo.save(incoming)
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
