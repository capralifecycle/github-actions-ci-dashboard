package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.cidashboard.persistence.createCiStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.Integration

@Integration
class DashboardUpdatesServiceTest {

  private val newestFirst: Comparator<CiStatus> = Comparator { a, b ->
    a.lastUpdatedAt.compareTo(b.lastUpdatedAt)
  }

  @Test
  fun `should get 20 last ci statuses and all failed statuses`() {
    // Given
    val statuses: List<CiStatus> = createStatuses()

    val repo = mockk<CiStatusRepo> { every { getAll() } returns statuses }
    val service = DashboardUpdatesService({ callback -> callback(repo) })

    // When
    val actualData = service.getUpdatedDashboardData("1")

    // Then
    verify { repo.getAll() }

    assertThat(actualData.allFailedBuilds)
        .hasSize(10)
        .allSatisfy { assertThat(it.lastStatus).isEqualTo(CiStatus.PipelineStatus.FAILED) }
        .isSortedAccordingTo(newestFirst)

    assertThat(actualData.lastBuilds).hasSizeLessThanOrEqualTo(20).isSortedAccordingTo(newestFirst)
  }

  /** @return List of 1 in-progress, 15 successes and 10 failed. */
  private fun createStatuses(): List<CiStatus> = buildList {
    val idCounter = AtomicInteger(2)

    repeat(10) {
      val id = idCounter.getAndIncrement().toString()
      add(
          createCiStatus(
              id,
              repoName = "repo-$id",
              lastStatus = CiStatus.PipelineStatus.FAILED,
              lastUpdatedAt = Instant.now()))
    }

    repeat(15) {
      val id = idCounter.getAndIncrement().toString()
      add(
          createCiStatus(
              id,
              repoName = "repo-$id",
              lastStatus = CiStatus.PipelineStatus.SUCCEEDED,
              lastUpdatedAt = Instant.now()))
    }

    add(
        createCiStatus(
            "1",
            repoName = "repo-1",
            lastStatus = CiStatus.PipelineStatus.IN_PROGRESS,
            lastUpdatedAt = Instant.now()))
  }
}
