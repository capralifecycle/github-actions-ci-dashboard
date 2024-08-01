package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import no.liflig.cidashboard.BranchMatcher
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.OrganizationMatcher
import no.liflig.cidashboard.RepositoryMatcher
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.cidashboard.persistence.DashboardConfigRepo
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

    val ciStatusRepo = mockk<CiStatusRepo> { every { getAll() } returns statuses }
    val configRepo = mockk<DashboardConfigRepo> { every { getById(any()) } returns null }
    val service =
        DashboardUpdatesService(
            { callback -> callback(ciStatusRepo) }, { callback -> callback(configRepo) })

    // When
    val actualData = service.getUpdatedDashboardData(DashboardConfigId("1"))

    // Then
    verify { ciStatusRepo.getAll() }

    assertThat(actualData.allFailedBuilds)
        .hasSize(10)
        .allSatisfy { assertThat(it.lastStatus).isEqualTo(CiStatus.PipelineStatus.FAILED) }
        .isSortedAccordingTo(newestFirst)

    assertThat(actualData.lastBuilds).hasSizeLessThanOrEqualTo(20).isSortedAccordingTo(newestFirst)
  }

  @Test
  fun `should filter statuses based on config`() {
    // Given
    val statuses: List<CiStatus> = createStatuses()

    val dashboardConfigId = DashboardConfigId("1")

    val dashboardConfig =
        DashboardConfig(
            dashboardConfigId,
            orgMatchers =
                listOf(
                    OrganizationMatcher(
                        "owner\\d".toRegex(),
                        listOf(
                            RepositoryMatcher(
                                "repo-.*".toRegex(),
                                listOf(BranchMatcher("bra.*\\d".toRegex())))))))

    val ciStatusRepo = mockk<CiStatusRepo> { every { getAll() } returns statuses }
    val configRepo =
        mockk<DashboardConfigRepo> { every { getById(dashboardConfigId) } returns dashboardConfig }
    val service =
        DashboardUpdatesService(
            { callback -> callback(ciStatusRepo) }, { callback -> callback(configRepo) })

    // When
    val actualData = service.getUpdatedDashboardData(dashboardConfigId)

    // Then
    verify { ciStatusRepo.getAll() }

    assertThat(actualData.allFailedBuilds).hasSize(0)

    assertThat(actualData.lastBuilds).hasSize(1)
    assertThat(actualData.lastBuilds[0].id.value).isEqualTo("1")
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
              repoOwner = "non-owner",
              branchName = "master",
              lastStatus = CiStatus.PipelineStatus.FAILED,
              lastUpdatedAt = Instant.now()))
    }

    repeat(15) {
      val id = idCounter.getAndIncrement().toString()
      add(
          createCiStatus(
              id,
              repoName = "repo-$id",
              repoOwner = "non-owner",
              branchName = "master",
              lastStatus = CiStatus.PipelineStatus.SUCCEEDED,
              lastUpdatedAt = Instant.now()))
    }

    add(
        createCiStatus(
            "1",
            repoName = "repo-1",
            repoOwner = "owner1",
            branchName = "branch1",
            lastStatus = CiStatus.PipelineStatus.IN_PROGRESS,
            lastUpdatedAt = Instant.now()))
  }
}
