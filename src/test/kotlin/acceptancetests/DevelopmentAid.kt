package acceptancetests

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.webhook.GitHubWebhookWorkflowRun
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.Integration
import test.util.WebhookPayload
import test.util.loadResource

@Integration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled("This is not a test.")
class DevelopmentAid {

  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension(fastPoll = false)
  }

  @Test
  @Disabled(
      "This is not a test. It is used to let developers spin up the project with sample data to develop CSS etc")
  fun `should ingest webhook data and present it to dashboards when they poll`() {
    val testWithManyRepos = true
    if (testWithManyRepos) {
      repeat(40) {
        infra.gitHub.sendWebhook(createPayload("repo-x$it", CiStatus.PipelineStatus.SUCCEEDED))
      }
      repeat(5) {
        infra.gitHub.sendWebhook(createPayload("repo-y$it", CiStatus.PipelineStatus.FAILED))
      }
    }

    infra.gitHub.sendWebhook(createPayload("repo-c", CiStatus.PipelineStatus.SUCCEEDED))
    infra.gitHub.sendWebhook(createPayload("repo-d", CiStatus.PipelineStatus.FAILED))
    infra.gitHub.sendWebhook(createPayload("repo-e", CiStatus.PipelineStatus.CANCELLED))

    // Give it a previous success runtime, to measure progress
    infra.gitHub.sendWebhook(createPayload("repo-b", CiStatus.PipelineStatus.SUCCEEDED))
    infra.gitHub.sendWebhook(
        createPayload("repo-b", CiStatus.PipelineStatus.IN_PROGRESS, startedAt = Instant.now()))

    infra.gitHub.sendWebhook(createPayload("repo-a", CiStatus.PipelineStatus.QUEUED))

    println(
        "\n".repeat(10) +
            "http://localhost:" +
            infra.app.config.apiOptions.serverPort.value +
            "/?token=${infra.app.config.apiOptions.clientSecretToken.value}")
    while (true) {
      println("Abort the test to stop infinite sleep...")
      Thread.sleep(10000)
    }
  }

  private fun createPayload(
      repoName: String,
      state: CiStatus.PipelineStatus,
      startedAt: Instant = Instant.now().minus(3, ChronoUnit.MINUTES)
  ): WebhookPayload {
    return object : WebhookPayload {
      override val type: String = "workflow_run"

      override fun asJson(): String {
        val payload =
            GitHubWebhookWorkflowRun.fromJson(
                loadResource(
                    when (state) {
                      CiStatus.PipelineStatus.QUEUED ->
                          "acceptancetests/webhook/user-workflow_run-requested.json"
                      CiStatus.PipelineStatus.IN_PROGRESS ->
                          "acceptancetests/webhook/user-workflow_run-in_progress.json"
                      CiStatus.PipelineStatus.FAILED ->
                          "acceptancetests/webhook/user-workflow_run-completed-failure.json"
                      CiStatus.PipelineStatus.CANCELLED ->
                          "acceptancetests/webhook/renovate-bot-workflow_run-completed-cancelled.json"
                      CiStatus.PipelineStatus.SUCCEEDED ->
                          "acceptancetests/webhook/renovate-bot-workflow_run-completed-success.json"
                    }))

        val newPayload =
            payload.copy(
                workflow = payload.workflow.copy(id = repoName.hashCode().toLong()),
                workflowRun =
                    payload.workflowRun.copy(updatedAt = Instant.now(), runStartedAt = startedAt),
                repository = payload.repository.copy(name = repoName))

        return Json.encodeToString(GitHubWebhookWorkflowRun.serializer(), newPayload)
      }
    }
  }
}
