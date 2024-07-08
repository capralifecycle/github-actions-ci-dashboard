package no.liflig.cidashboard.webhook

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import test.util.Integration
import test.util.IntegrationTest
import test.util.loadResource

@Integration
class IncomingWebhookServiceTest {

  @Test
  fun `should do nothing on ping`() {
    // Given
    val service = IncomingWebhookService()

    // When
    service.handlePing(
        GitHubWebhookPing.fromJson(loadResource("acceptancetests/webhook/github-ping-body.json")))

    // Then
    // Nothing. Should just log.
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Workflow Run")
  inner class WorkflowRun {

    /** We always keep the latest data, because if reflects the current workflow state. */
    @IntegrationTest
    fun `should persist workflow_run when the received data is newer`() {
      // Database is empty

      // Create event

      // Send event to service

      // Query database for newer event
    }

    /**
     * If the database has an event with data that is newer than the incoming, we just keep the
     * database's data. Might happen if two servers run and webhooks are triggered rapidly. Or it
     * might happen on retry caused by a failure.
     */
    @IntegrationTest
    fun `should discard outdated workflow_run events`() {
      TODO("Not yet implemented")
    }
  }
}
