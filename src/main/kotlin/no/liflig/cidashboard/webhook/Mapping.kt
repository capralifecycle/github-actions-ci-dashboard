package no.liflig.cidashboard.webhook

import no.liflig.cidashboard.persistence.BranchName
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus
import no.liflig.cidashboard.persistence.Commit
import no.liflig.cidashboard.persistence.Repo
import no.liflig.cidashboard.persistence.RepoId
import no.liflig.cidashboard.persistence.RepoName
import no.liflig.cidashboard.persistence.User
import no.liflig.cidashboard.persistence.UserId
import no.liflig.cidashboard.persistence.Username

fun GitHubWebhookWorkflowRun.toCiStatus() =
    CiStatus(
        id = workflow.id.toString(),
        repo =
            Repo(
                id = RepoId(repository.id),
                name = RepoName(repository.name),
                owner = Username(repository.owner.login),
                defaultBranch = BranchName(repository.defaultBranch),
            ),
        branch = BranchName(workflowRun.headBranch),
        lastStatus =
            when (action) {
              GitHubWebhookWorkflowRun.Action.Requested -> PipelineStatus.QUEUED
              GitHubWebhookWorkflowRun.Action.InProgress -> PipelineStatus.IN_PROGRESS
              GitHubWebhookWorkflowRun.Action.Completed ->
                  when (workflowRun.conclusion) {
                    null,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.ActionRequired ->
                        PipelineStatus.IN_PROGRESS
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Cancelled,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Neutral,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Skipped,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Stale,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.TimedOut,
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Failure -> PipelineStatus.FAILED
                    GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Success ->
                        PipelineStatus.SUCCEEDED
                  }
            },
        lastUpdatedAt = workflowRun.updatedAt,
        lastCommit =
            Commit(
                sha = workflowRun.headCommit.id,
                commitDate = workflowRun.headCommit.timestamp,
                title = workflowRun.displayTitle,
                message = workflowRun.headCommit.message,
                commiter =
                    User(
                        id = UserId(workflowRun.actor.id),
                        username = Username(workflowRun.actor.login),
                        avatarUrl = workflowRun.actor.avatarUrl)),
        triggeredBy = Username(workflowRun.triggeringActor.login),
        lastSuccessfulCommit = null,
    )
