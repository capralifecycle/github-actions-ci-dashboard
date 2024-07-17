package no.liflig.cidashboard.webhook

/** A list of case-sensitive names to keep when receiving events in [IncomingWebhookService]. */
@JvmInline value class BranchWhitelist(val value: List<String>)

/**
 * Inspects a workflow_run event and instructs if it should be ignored or not.
 *
 * CALS-820
 */
object WhitelistBranchPolicy {
  fun shouldIgnoreEvent(
      workflowRun: GitHubWebhookWorkflowRun,
      branchWhitelist: BranchWhitelist
  ): Boolean {
    if (branchWhitelist.value.isEmpty()) {
      return false
    }

    val branch = workflowRun.workflowRun.headBranch
    val isWhitelisted = branchWhitelist.value.contains(branch)

    return !isWhitelisted
  }
}
