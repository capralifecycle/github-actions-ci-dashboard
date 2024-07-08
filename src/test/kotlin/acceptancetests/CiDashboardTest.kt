package acceptancetests

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.Integration
import test.util.WebhookPayload

@Integration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CI Dashboard")
class CiDashboardTest {

  companion object {
    @JvmField @RegisterExtension val infra = AcceptanceTestExtension()
  }

  @Test
  fun `should ingest webhook data and present it to dashboards when they poll`() {
    infra.tvBrowser.navigateToDashboard()
    infra.tvBrowser.verifyDashboardIsEmpty()

    infra.gitHub.sendWebhook(WebhookPayload.ExampleRepo.WORKFLOW_RUN_1_REQUESTED)
    infra.gitHub.sendWebhook(WebhookPayload.ExampleRepo.WORKFLOW_RUN_1_IN_PROGRESS)
    infra.tvBrowser.verifyDashboardHasRepoInProgress(WebhookPayload.ExampleRepo.repoName)

    infra.gitHub.sendWebhook(WebhookPayload.ExampleRepo.WORKFLOW_RUN_1_SUCCESS)
    infra.tvBrowser.verifyDashboardHasRepoInProgress(WebhookPayload.ExampleRepo.repoName)
  }
}
