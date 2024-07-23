package no.liflig.cidashboard.persistence

import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import no.liflig.snapshot.verifyJsonSnapshot
import org.junit.jupiter.api.Test

class CiStatusTest {

  @Test
  fun `should serialize to json`() {
    // Given
    val ciStatus =
        CiStatus(
            id = CiStatusId("1"),
            repo =
                Repo(
                    id = RepoId(2),
                    name = RepoName("test-repo"),
                    owner = Username("my-orgname"),
                    defaultBranch = BranchName("master")),
            branch = BranchName("master"),
            lastStatus = CiStatus.PipelineStatus.SUCCEEDED,
            startedAt = Instant.parse("2024-07-05T12:20:40Z"),
            lastUpdatedAt = Instant.parse("2024-07-05T12:25:40Z"),
            lastCommit =
                Commit(
                    sha = "123abc",
                    commitDate = Instant.parse("2024-07-05T12:25:40Z"),
                    title = "Add feature",
                    message = "Add feature\nThis is helpful",
                    commiter =
                        User(
                            id = UserId(5),
                            username = Username("krissrex"),
                            avatarUrl = "https://avatars.githubusercontent.com/u/7364831?v=4")),
            triggeredBy = Username("krissrex"),
            lastSuccessfulCommit =
                Commit(
                    sha = "456def",
                    commitDate = Instant.parse("2024-07-03T10:00:00Z"),
                    title = "Initial commit",
                    message = "Initial commit",
                    commiter =
                        User(id = UserId(9), username = Username("mikaelthd"), avatarUrl = "")),
            durationOfLastSuccess = 5.minutes)

    // When
    val json = ciStatus.toJson()

    // Then

    // Changes to the structure may require database migrations!
    verifyJsonSnapshot("persistence/ci-status.json", json)
  }
}
