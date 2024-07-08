package no.liflig.cidashboard.webhook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.loadResource

class GitHubWebhookWorkflowRunTest {
  @Test
  fun `should deserialize requested type`() {
    // Given
    val workflowEventJson = loadResource("acceptancetests/webhook/user-workflow_run-requested.json")

    // When
    val event = GitHubWebhookWorkflowRun.fromJson(workflowEventJson)

    // Then
    assertThat(event.action).isEqualTo(GitHubWebhookWorkflowRun.Action.Requested)
    assertThat(event.workflowRun.name).isEqualTo("ci")
    assertThat(event.sender.login).isEqualTo("krissrex")
  }

  @Test
  fun `should deserialize in-progress`() {
    // Given
    val workflowEventJson =
        loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")

    // When
    val event = GitHubWebhookWorkflowRun.fromJson(workflowEventJson)

    // Then
    assertThat(event.action).isEqualTo(GitHubWebhookWorkflowRun.Action.InProgress)
    assertThat(event.workflowRun.name).isEqualTo("ci")
    assertThat(event.sender.login).isEqualTo("krissrex")
  }

  @Test
  fun `should deserialize completed`() {
    // Given
    val workflowEventJson = loadResource("acceptancetests/webhook/user-workflow_run-completed.json")

    // When
    val event = GitHubWebhookWorkflowRun.fromJson(workflowEventJson)

    // Then
    assertThat(event.action).isEqualTo(GitHubWebhookWorkflowRun.Action.Completed)
    assertThat(event.workflowRun.name).isEqualTo("ci")
    assertThat(event.sender.login).isEqualTo("krissrex")
  }
}
