package no.liflig.cidashboard.admin.gui

import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.OrganizationMatcher
import no.liflig.cidashboard.persistence.BranchName
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.CiStatusId
import no.liflig.cidashboard.persistence.CiStatusRepo
import no.liflig.cidashboard.persistence.Commit
import no.liflig.cidashboard.persistence.DashboardConfigRepo
import no.liflig.cidashboard.persistence.Repo
import no.liflig.cidashboard.persistence.RepoId
import no.liflig.cidashboard.persistence.RepoName
import no.liflig.cidashboard.persistence.User
import no.liflig.cidashboard.persistence.UserId
import no.liflig.cidashboard.persistence.Username
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AdminGuiServiceTest {

  @Test
  fun `should map ci statuses to rows`() {
    val now = Instant.now()
    val user =
        User(
            id = UserId(1),
            username = Username("testuser"),
            avatarUrl = "https://example.com/avatar.png",
        )
    val commit =
        Commit(
            sha = "abc123",
            commitDate = now,
            title = "Test commit",
            message = "Test message",
            commiter = user,
        )
    val statuses =
        listOf(
            CiStatus(
                id = CiStatusId("status-1"),
                repo =
                    Repo(
                        id = RepoId(1),
                        owner = Username("my-org"),
                        name = RepoName("my-repo"),
                        defaultBranch = BranchName("main"),
                    ),
                branch = BranchName("main"),
                lastStatus = CiStatus.PipelineStatus.SUCCEEDED,
                startedAt = now,
                lastUpdatedAt = now,
                buildNumber = 1,
                lastCommit = commit,
                triggeredBy = Username("testuser"),
            ),
            CiStatus(
                id = CiStatusId("status-2"),
                repo =
                    Repo(
                        id = RepoId(2),
                        owner = Username("other-org"),
                        name = RepoName("other-repo"),
                        defaultBranch = BranchName("develop"),
                    ),
                branch = BranchName("develop"),
                lastStatus = CiStatus.PipelineStatus.FAILED,
                startedAt = now,
                lastUpdatedAt = now.plusSeconds(60),
                buildNumber = 2,
                lastCommit = commit,
                triggeredBy = Username("testuser"),
            ),
        )
    val ciStatusRepo = mockk<CiStatusRepo> { every { getAll() } returns statuses }
    val dashboardConfigRepo = mockk<DashboardConfigRepo> { every { getAll() } returns emptyList() }

    val service =
        AdminGuiService(
            ciStatusRepo = { callback -> callback(ciStatusRepo) },
            configRepo = { callback -> callback(dashboardConfigRepo) },
        )
    val rows = service.getCiStatuses()

    assertThat(rows).hasSize(2)
    assertThat(rows[0].id).isEqualTo("status-1")
    assertThat(rows[0].repoOwner).isEqualTo("my-org")
    assertThat(rows[0].repoName).isEqualTo("my-repo")
    assertThat(rows[0].branch).isEqualTo("main")
    assertThat(rows[0].status).isEqualTo("SUCCEEDED")
    assertThat(rows[1].id).isEqualTo("status-2")
    assertThat(rows[1].status).isEqualTo("FAILED")
  }

  @Test
  fun `should map configs to rows`() {
    val configs =
        listOf(
            DashboardConfig(
                id = DashboardConfigId("config-1"),
                displayName = "My Dashboard",
                orgMatchers =
                    listOf(
                        OrganizationMatcher(Regex("org-1")),
                        OrganizationMatcher(Regex("org-2")),
                    ),
            ),
        )
    val ciStatusRepo = mockk<CiStatusRepo> { every { getAll() } returns emptyList() }
    val dashboardConfigRepo = mockk<DashboardConfigRepo> { every { getAll() } returns configs }

    val service =
        AdminGuiService(
            ciStatusRepo = { callback -> callback(ciStatusRepo) },
            configRepo = { callback -> callback(dashboardConfigRepo) },
        )
    val rows = service.getConfigs()

    assertThat(rows).hasSize(1)
    assertThat(rows[0].id).isEqualTo("config-1")
    assertThat(rows[0].displayName).isEqualTo("My Dashboard")
    assertThat(rows[0].orgMatchers).isEqualTo("org-1, org-2")
  }

  @Test
  fun `should handle empty lists`() {
    val service = AdminGuiService(ciStatusRepo = { emptyList() }, configRepo = { emptyList() })

    assertThat(service.getCiStatuses()).isEmpty()
    assertThat(service.getConfigs()).isEmpty()
  }
}
