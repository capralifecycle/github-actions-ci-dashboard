package no.liflig.cidashboard.webhook

import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.liflig.cidashboard.persistence.CiStatusRepo
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
    val inTransaction = mockk<Transaction>()
    val service = IncomingWebhookService(inTransaction)

    // When
    service.handlePing(
        GitHubWebhookPing.fromJson(loadResource("acceptancetests/webhook/github-ping-body.json")))

    // Then
    // Nothing. Should just log.
    verify { inTransaction wasNot Called }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Workflow Run")
  inner class WorkflowRun {

    /** We always keep the latest data, because if reflects the current workflow state. */
    @IntegrationTest
    fun `should persist workflow_run when the received data is newer`() {
      // Database is empty
      val repo =
          mockk<CiStatusRepo> {
            every { save(any()) } returns Unit
            every { getById(any()) } returns null
          }

      // Create event
      val workflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-completed-failure.json"))

      // Send event to service
      val inTransaction = Transaction { callback -> callback(repo) }
      val service = IncomingWebhookService(inTransaction)
      service.handleWorkflowRun(workflowRun)

      verify { repo.save(any()) }
    }

    /**
     * If the database has an event with data that is newer than the incoming, we just keep the
     * database's data. Might happen if two servers run and webhooks are triggered rapidly. Or it
     * might happen on retry caused by a failure.
     */
    @IntegrationTest
    fun `should discard outdated workflow_run events`() {
      // Database has newer event
      val newWorkflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-completed-failure.json"))

      val repo =
          mockk<CiStatusRepo> {
            every { save(any()) } returns Unit
            every { getById(newWorkflowRun.workflow.id) } returns newWorkflowRun.toCiStatus()
          }

      // Create event
      val outdatedWorkflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json"))

      // Send event to service
      val inTransaction = Transaction { callback -> callback(repo) }
      val service = IncomingWebhookService(inTransaction)
      service.handleWorkflowRun(outdatedWorkflowRun)

      verify(exactly = 0) { repo.save(any()) }
    }
  }
}
