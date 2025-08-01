package acceptancetests

import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.FAILED
import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.IN_PROGRESS
import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.QUEUED
import no.liflig.cidashboard.persistence.CiStatus.PipelineStatus.SUCCEEDED
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
import test.util.AcceptanceTestExtension
import test.util.FileWebhookPayload
import test.util.Integration

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

    infra.gitHub.sendWebhook(FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_REQUESTED)
    infra.tvBrowser.verifyDashboardHasRepoInStatus(FileWebhookPayload.ExampleRepo.repoName, QUEUED)

    infra.gitHub.sendWebhook(FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_IN_PROGRESS)
    infra.tvBrowser.verifyDashboardHasRepoInStatus(
        FileWebhookPayload.ExampleRepo.repoName, IN_PROGRESS)

    infra.gitHub.sendWebhook(FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_COMPLETED_FAILURE)
    infra.tvBrowser.verifyDashboardHasRepoInStatus(FileWebhookPayload.ExampleRepo.repoName, FAILED)
    infra.tvBrowser.verifyAllFailedBuildsIsListingRepo(FileWebhookPayload.ExampleRepo.repoName)

    infra.gitHub.sendWebhook(FileWebhookPayload.ExampleRepo.WORKFLOW_RUN_1_COMPLETED_SUCCESS)
    infra.tvBrowser.verifyDashboardHasRepoInStatus(
        FileWebhookPayload.ExampleRepo.repoName, SUCCEEDED)

    // Create a screenshot for Readme with some more data.
    infra.gitHub.sendWebhook(
        FileWebhookPayload.DataForScreenshots.LIFLIG_PROPERTIES_WORKFLOW_RUN_1_COMPLETED_FAILURE)
    infra.tvBrowser.verifyAllFailedBuildsIsListingRepo(
        FileWebhookPayload.DataForScreenshots.repoNameLifligProperties)
    infra.gitHub.sendWebhook(
        FileWebhookPayload.DataForScreenshots.LIFLIG_CDK_WORKFLOW_RUN_2_IN_PROGRESS)
    infra.tvBrowser.verifyDashboardHasRepoInStatus(
        FileWebhookPayload.DataForScreenshots.repoNameLifligCdk, IN_PROGRESS)
    infra.tvBrowser.saveScreenshotForReadme()
  }
}
