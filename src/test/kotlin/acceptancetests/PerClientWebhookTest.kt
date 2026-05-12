package acceptancetests

import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.QUEUED
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.FileWebhookPayload
import test.util.Integration
import test.util.TestConstants.TEST_CLIENT_ID
import test.util.TestConstants.TEST_CLIENT_SECRET

@Integration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Per-client webhook")
class PerClientWebhookTest {

  companion object {
    @JvmField
    @RegisterExtension
    val infra =
        AcceptanceTestExtension(
            clientSecrets = mapOf(TEST_CLIENT_ID to TEST_CLIENT_SECRET),
        )
  }

  @Test
  fun `webhook is accepted and status becomes visible`() {
    infra.tvBrowser.navigateToDashboard()
    infra.tvBrowser.verifyDashboardIsEmpty()

    infra.gitHub.sendWebhook(
        FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_REQUESTED,
        TEST_CLIENT_ID,
    )
    infra.tvBrowser.verifyDashboardHasRepoInStatus(FileWebhookPayload.ExampleRepo.repoName, QUEUED)
  }
}
