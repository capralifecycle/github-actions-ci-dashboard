package no.liflig.cidashboard.webhook

@JvmInline value class WorkflowNameWhitelist(val value: List<String>)

/**
 * Inspects a workflow_run event and instructs if it should be ignored or not.
 *
 * CALS-820
 */
object WhitelistWorkflowNamePolicy {
  fun shouldIgnore(
      workflowRun: GitHubWebhookWorkflowRun,
      whitelist: WorkflowNameWhitelist,
  ): Boolean {
    if (whitelist.value.isEmpty()) {
      return false
    }

    val workflowName = workflowRun.workflowRun.name
    val isWhitelisted = whitelist.value.contains(workflowName)

    return !isWhitelisted
  }
}
