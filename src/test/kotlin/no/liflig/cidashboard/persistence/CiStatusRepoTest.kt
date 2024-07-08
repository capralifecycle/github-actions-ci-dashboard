package no.liflig.cidashboard.persistence

import java.time.Instant
import no.liflig.cidashboard.common.config.Config
import no.liflig.cidashboard.common.database.DatabaseConfigurator
import no.liflig.cidashboard.common.database.DbPassword
import no.liflig.cidashboard.common.database.DbUrl
import no.liflig.cidashboard.common.database.DbUsername
import org.assertj.core.api.Assertions.assertThat
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import test.util.AcceptanceTestExtension
import test.util.Integration
import test.util.IntegrationTest

@Integration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CiStatusRepoTest {

  companion object {
    private val database = AcceptanceTestExtension.Database()
    lateinit var jdbi: Jdbi

    @BeforeAll
    @JvmStatic
    fun beforeAll() {
      database.start()

      val config = Config.load().let { database.applyTo(it) }
      jdbi =
          DatabaseConfigurator.createJdbiInstance(
              DbUrl(config.database.jdbcUrl),
              DbUsername(config.database.username),
              DbPassword(config.database.password))
    }

    @AfterAll
    @JvmStatic
    fun afterAll() {
      database.stop()
    }
  }

  @BeforeEach
  fun beforeEach() {
    database.clearAllData()
  }

  @IntegrationTest
  fun `should persist to database`() {
    // Given
    val ciStatus = createCiStatus(id = "1")

    // When
    jdbi.useHandle<Exception> { handle ->
      val repo = CiStatusRepo(handle)

      repo.save(ciStatus)
    }

    // Then
    val rowCount =
        jdbi.withHandle<Int, Exception> { handle ->
          handle
              .select("SELECT 1 FROM ${CiStatusRepo.TABLE_NAME} WHERE id = :id")
              .bind("id", ciStatus.id)
              .mapTo(Int::class.java)
              .first()
        }

    assertThat(rowCount).isEqualTo(1)
  }

  @IntegrationTest
  fun `should read from database`() {
    // Given
    val ciStatus = createCiStatus(id = "2")
    jdbi.useHandle<Exception> { handle -> CiStatusRepo(handle).save(ciStatus) }

    // When
    val actualResult: List<CiStatus> =
        jdbi.withHandle<List<CiStatus>, Exception> { handle -> CiStatusRepo(handle).getAll() }

    // Then
    assertThat(actualResult).hasSize(1).contains(ciStatus)
  }
}

private fun createCiStatus(id: String): CiStatus {
  return CiStatus(
      id = id,
      repo =
          Repo(
              id = RepoId(2),
              name = RepoName("test-repo"),
              owner = Username("my-orgname"),
              defaultBranch = BranchName("master")),
      branch = BranchName("master"),
      lastStatus = CiStatus.PipelineStatus.SUCCEEDED,
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
      lastSuccessfulCommit = null,
  )
}
