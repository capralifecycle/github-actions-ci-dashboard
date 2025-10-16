package no.liflig.cidashboard.webhook

import io.mockk.Called
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.cidashboard.persistence.createCiStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import test.util.loadResource

class IncomingWebhookServiceTest {

  @Test
  fun `should do nothing on ping`() {
    // Given
    val inTransaction = mockk<CiStatusTransaction>()
    val service = IncomingWebhookService(inTransaction)

    // When
    service.handlePing(
        GitHubWebhookPing.fromJson(loadResource("acceptancetests/webhook/github-ping-body.json"))
    )

    // Then
    // Nothing. Should just log.
    verify { inTransaction wasNot Called }
  }

  @Nested
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @DisplayName("Workflow Run")
  inner class WorkflowRun {

    private val repo: CiStatusRepo = mockk()
    private val inTransaction = CiStatusTransaction { callback -> callback(repo) }
    private val service = IncomingWebhookService(inTransaction)

    @BeforeEach
    fun setUp() {
      clearMocks(repo)

      every { repo.save(any()) } just Runs
      // Database is empty:
      every { repo.getById(any()) } returns null
    }

    /** We always keep the latest data, because if reflects the current workflow state. */
    @Test
    fun `should persist workflow_run when the received data is newer`() {
      // Create event
      val workflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-completed-failure.json")
          )

      // Send event to service
      service.handleWorkflowRun(workflowRun)

      verify { repo.save(any()) }
    }

    /**
     * If the database has an event with data that is newer than the incoming, we just keep the
     * database's data. Might happen if two servers run and webhooks are triggered rapidly. Or it
     * might happen on retry caused by a failure.
     */
    @Test
    fun `should discard outdated workflow_run events`() {
      // Database has newer event
      val newWorkflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-completed-failure.json")
          )

      every { repo.getById(CiStatusId.from(newWorkflowRun)) } returns newWorkflowRun.toCiStatus()

      // Create event
      val outdatedWorkflowRun =
          GitHubWebhookWorkflowRun.fromJson(
              loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")
          )

      // Send event to service
      service.handleWorkflowRun(outdatedWorkflowRun)

      // Should not be saved
      verify(exactly = 0) { repo.save(any()) }
    }

    /**
     * Every time a webhook with success comes in, we can persist the total time of the build. This
     * total time is used to show a progress bar on the next in-progress builds.
     *
     * Whenever a webhook event with a conclusion not equal to success comes in, we just keep the
     * old duration.
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Duration Of Last Success")
    inner class DurationOfLastSuccess {
      @Test
      fun `should persist build duration when event is success`() {
        // Create event
        val workflowRun =
            GitHubWebhookWorkflowRun.fromJson(
                loadResource(
                    "acceptancetests/webhook/renovate-bot-workflow_run-completed-success.json"
                )
            )

        // Send event to service
        service.handleWorkflowRun(workflowRun)

        // Verify it set a duration
        val saved = slot<CiStatus>()
        verify { repo.save(capture(saved)) }

        assertThat(saved.captured.durationOfLastSuccess).isEqualTo(193.seconds)
      }

      @Test
      fun `should keep the old status when new event is not success`() {
        // The existing state with a duration
        val oldDuration = 2.minutes
        every { repo.getById(CiStatusId("105563496-master")) } returns
            createCiStatus(
                "105563496-master",
                repoName = "github-actions-ci-dashboard",
                durationOfLastSuccess = oldDuration,
            )

        // Send new event with in-progress
        val inProgressWorkflowRun =
            GitHubWebhookWorkflowRun.fromJson(
                loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")
            )

        service.handleWorkflowRun(inProgressWorkflowRun)

        // Should still have a 2-minute duration
        val saved = slot<CiStatus>()
        verify { repo.save(capture(saved)) }

        assertThat(saved.captured.durationOfLastSuccess).isEqualTo(oldDuration)
      }
    }

    /**
     * If two CI runs are started in rapid succession because you commit and push quickly, the older
     * commit's workflow _could_ finish after the head-commit's workflow. But we want the status for
     * the head of the branch, so the events for older workflow runs must be discarded.
     *
     * (Maybe it's more correct to use the build's HeadCommit timestamp?)
     */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Build Number")
    inner class BuildNumber {
      private val existingCiStatus =
          createCiStatus(
              "105563496-master",
              repoName = "github-actions-ci-dashboard",
              buildNumber = 10,
          )

      @Test
      fun `should discard events from lower build numbers than the persisted status`() {
        // Given
        every { repo.getById(CiStatusId("105563496-master")) } returns existingCiStatus

        val oldBuildNumberEvent = createWorkflowRunEventWithBuildNumber(9)

        // When
        service.handleWorkflowRun(oldBuildNumberEvent)

        // Then
        verify(inverse = true) { repo.save(any()) }
      }

      @Test
      fun `should persist events for build numbers equal to the previously persisted status`() {
        // Given
        every { repo.getById(CiStatusId("105563496-master")) } returns existingCiStatus

        val sameBuildNumberEvent = createWorkflowRunEventWithBuildNumber(10)

        // When
        service.handleWorkflowRun(sameBuildNumberEvent)

        // Then
        verify { repo.save(any()) }
      }

      @Test
      fun `should persist events for build numbers higher than the previously persisted status`() {
        // Given
        every { repo.getById(CiStatusId("105563496-master")) } returns existingCiStatus

        val newBuildNumberEvent = createWorkflowRunEventWithBuildNumber(11)

        // When
        service.handleWorkflowRun(newBuildNumberEvent)

        // Then
        verify { repo.save(any()) }
      }

      private fun createWorkflowRunEventWithBuildNumber(number: Long) =
          GitHubWebhookWorkflowRun.fromJson(
                  loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")
              )
              .let { event ->
                event.copy(
                    workflowRun =
                        event.workflowRun.copy(runNumber = number, updatedAt = Instant.now())
                )
              }
    }

    /** CALS-820 */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Branch Whitelist")
    inner class BranchWhitelist {
      @Test
      fun `should persist events for branches in the whitelist`() {
        // Given
        val workflowRun = createWorkflowRunEventFor("master")

        val whitelist = BranchWhitelist(listOf("master", "main"))
        val service = IncomingWebhookService(inTransaction, branchWhitelist = whitelist)

        // When
        service.handleWorkflowRun(workflowRun)

        // Then
        verify { repo.save(any()) }
      }

      @Test
      fun `should discard events for branches not in whitelist`() {
        // Given
        val workflowRun = createWorkflowRunEventFor("feat/my-branch")

        val whitelist = BranchWhitelist(listOf("master", "main"))
        val service = IncomingWebhookService(inTransaction, branchWhitelist = whitelist)

        // When
        service.handleWorkflowRun(workflowRun)

        // Then
        verify(inverse = true) { repo.save(any()) }
      }

      private fun createWorkflowRunEventFor(branch: String) =
          GitHubWebhookWorkflowRun.fromJson(
                  loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")
              )
              .let { event ->
                event.copy(workflowRun = event.workflowRun.copy(headBranch = branch))
              }
    }

    /** CALS-820 */
    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Workflow Name Whitelist")
    inner class WorkflowNameWhitelist {

      @Test
      fun `should persist events for workflow names in the whitelist`() {
        // Given
        val workflowRun = createWorkflowRunEventWithName("ci")

        val whitelist = WorkflowNameWhitelist(listOf("ci"))
        val service = IncomingWebhookService(inTransaction, workflowNameWhitelist = whitelist)

        // When
        service.handleWorkflowRun(workflowRun)

        // Then
        verify { repo.save(any()) }
      }

      @Test
      fun `should discard events for workflow names not in the whitelist`() {
        // Given
        val workflowRun = createWorkflowRunEventWithName("my-workflow")

        val whitelist = WorkflowNameWhitelist(listOf("ci"))
        val service = IncomingWebhookService(inTransaction, workflowNameWhitelist = whitelist)

        // When
        service.handleWorkflowRun(workflowRun)

        // Then
        verify(inverse = true) { repo.save(any()) }
      }

      private fun createWorkflowRunEventWithName(workflowName: String) =
          GitHubWebhookWorkflowRun.fromJson(
                  loadResource("acceptancetests/webhook/user-workflow_run-in_progress.json")
              )
              .let { event ->
                event.copy(
                    workflowRun = event.workflowRun.copy(name = workflowName),
                    workflow =
                        event.workflow.copy(
                            name = workflowName,
                            path = ".github/workflows/${workflowName}.yaml",
                        ),
                )
              }
    }
  }
}
