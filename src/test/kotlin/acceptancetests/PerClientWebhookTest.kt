package acceptancetests

import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.QUEUED
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.FileWebhookPayload
import test.util.Integration

@Integration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Per-client webhook")
class PerClientWebhookTest {

  companion object {
    const val TEST_CLIENT_ID = "test-org"
    const val TEST_CLIENT_SECRET = "test-client-secret"

    @JvmField
    @RegisterExtension
    val infra =
        AcceptanceTestExtension(
            clientSecrets = mapOf(TEST_CLIENT_ID to TEST_CLIENT_SECRET),
        )
  }

  @Test
  fun `should accept webhook via per-client endpoint`() {
    infra.tvBrowser.navigateToDashboard()
    infra.tvBrowser.verifyDashboardIsEmpty()

    infra.gitHub.sendWebhookWithClientId(
        FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_REQUESTED,
        TEST_CLIENT_ID,
    )
    infra.tvBrowser.verifyDashboardHasRepoInStatus(FileWebhookPayload.ExampleRepo.repoName, QUEUED)
  }
}
