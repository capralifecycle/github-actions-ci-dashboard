package no.liflig.cidashboard.webhook

import java.time.Instant
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import no.liflig.cidashboard.persistence.BranchName
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.cidashboard.persistence.Commit
import no.liflig.cidashboard.persistence.Repo
import no.liflig.cidashboard.persistence.RepoId
import no.liflig.cidashboard.persistence.RepoName
import no.liflig.cidashboard.persistence.User
import no.liflig.cidashboard.persistence.UserId
import no.liflig.cidashboard.persistence.Username
import no.liflig.cidashboard.webhook.GitHubWebhookWorkflowRun.WorkflowRun.Conclusion.Success

fun GitHubWebhookWorkflowRun.toCiStatus() =
    CiStatus(
        id = CiStatusId.from(this),
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
                    Success -> PipelineStatus.SUCCEEDED
                  }
            },
        startedAt = workflowRun.runStartedAt,
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
        buildNumber = workflowRun.runNumber,
        durationOfLastSuccess =
            if (workflowRun.conclusion == Success) {
              timeBetween(workflowRun.runStartedAt, workflowRun.updatedAt)
            } else {
              null
            })

fun CiStatusId.Companion.from(workflowRunEvent: GitHubWebhookWorkflowRun): CiStatusId =
    // Not sure if we need more identifiers. Is the workflow id unique per fork of a repo?
    CiStatusId("${workflowRunEvent.workflow.id}-${workflowRunEvent.workflowRun.headBranch}")

private fun timeBetween(start: Instant, stop: Instant): Duration =
    abs(stop.toEpochMilli() - start.toEpochMilli()).milliseconds
