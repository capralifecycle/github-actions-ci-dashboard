package acceptancetests

import java.time.Instant
import kotlin.random.Random
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
    infra.gitHub.sendWebhook(createPayload("repo-a", CiStatus.PipelineStatus.QUEUED))
    infra.gitHub.sendWebhook(createPayload("repo-b", CiStatus.PipelineStatus.IN_PROGRESS))
    infra.gitHub.sendWebhook(createPayload("repo-c", CiStatus.PipelineStatus.SUCCEEDED))
    infra.gitHub.sendWebhook(createPayload("repo-d", CiStatus.PipelineStatus.FAILED))

    val testWithManyRepos = false
    if (testWithManyRepos) {
      repeat(40) {
        infra.gitHub.sendWebhook(createPayload("repo-x$it", CiStatus.PipelineStatus.SUCCEEDED))
      }
      repeat(10) {
        infra.gitHub.sendWebhook(createPayload("repo-y$it", CiStatus.PipelineStatus.FAILED))
      }
    }

    println(
        "\n".repeat(10) +
            "http://localhost:" +
            infra.app.config.apiOptions.serverPort.value +
            "/?token=todo-add-token-via-config-api")
    while (true) {
      println("Abort the test to stop infinite sleep...")
      Thread.sleep(10000)
    }
  }

  private fun createPayload(repoName: String, state: CiStatus.PipelineStatus): WebhookPayload {
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
                          "acceptancetests/webhook/renovate-bot-workflow_run-completed-success.json"
                      CiStatus.PipelineStatus.SUCCEEDED ->
                          "acceptancetests/webhook/user-workflow_run-completed-failure.json"
                    }))

        val newPayload =
            payload.copy(
                workflow = payload.workflow.copy(id = Random.nextLong()),
                workflowRun = payload.workflowRun.copy(updatedAt = Instant.now()),
                repository = payload.repository.copy(name = repoName))

        return Json.encodeToString(GitHubWebhookWorkflowRun.serializer(), newPayload)
      }
    }
  }
}
