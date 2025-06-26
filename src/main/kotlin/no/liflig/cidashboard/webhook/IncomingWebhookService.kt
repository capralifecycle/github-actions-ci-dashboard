package no.liflig.cidashboard.webhook

import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.logging.getLogger
import org.jdbi.v3.core.Jdbi

/**
 * Handles events and decides what to do with them, like storing to a repo or not.
 *
 * CALS-821
 */
class IncomingWebhookService(
    private val inTransaction: CiStatusTransaction,
    /**
     * If non-empty, the events in [handleWorkflowRun] will only be persisted if the branch is
     * exactly contained in this list. It is case-sensitive.
     */
    private val branchWhitelist: BranchWhitelist = BranchWhitelist(emptyList()),
    private val workflowNameWhitelist: WorkflowNameWhitelist = WorkflowNameWhitelist(emptyList()),
) {

  companion object {
    private val log = getLogger()
  }

  fun handlePing(ping: GitHubWebhookPing) {
    log.info {
      field("webhook.event", ping)
      "Got ping from GitHub"
    }
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

      if (DiscardOutdatedEventsPolicy.shouldDiscard(existing, incoming)) {
        return@inTransaction
      }
      if (DiscardLowerBuildNumberStatusesPolicy.shouldDiscard(existing, incoming)) {
        return@inTransaction
      }

      if (incoming.lastStatus != CiStatus.PipelineStatus.SUCCEEDED) {
        // Keep old build duration
        incoming = incoming.copy(durationOfLastSuccess = existing?.durationOfLastSuccess)
      }

      repo.save(incoming)
    }
  }
}

fun interface CiStatusTransaction {
  operator fun invoke(block: (CiStatusRepo) -> Unit)
}

class JdbiCiStatusTransaction(private val jdbi: Jdbi) : CiStatusTransaction {
  override fun invoke(block: (CiStatusRepo) -> Unit) {
    jdbi.useTransaction<Exception> { handle -> block(CiStatusRepo(handle)) }
  }
}
