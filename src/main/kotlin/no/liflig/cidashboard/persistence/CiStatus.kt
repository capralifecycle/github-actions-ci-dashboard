@file:UseSerializers(InstantSerializer::class)

package no.liflig.cidashboard.persistence

import java.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.common.serialization.InstantSerializer

@Persisted
@Serializable
data class CiStatus(
    /** Primary key */
    val id: String,
    val repo: Repo,
    val branch: BranchName,
    val lastStatus: PipelineStatus,
    /** The timestamp of the workflow event that updated this [lastStatus]. */
    val lastUpdatedAt: Instant,
    val lastCommit: Commit,
    val triggeredBy: Username,
    val lastSuccessfulCommit: Commit? = null,
) {

  companion object {
    private val json = Json {
      ignoreUnknownKeys = false
      encodeDefaults = true
    }

    fun fromJson(jsonString: String): CiStatus = json.decodeFromString(serializer(), jsonString)
  }

  fun toJson(): String = json.encodeToString(serializer(), this)

  @Serializable
  enum class PipelineStatus {
    QUEUED,
    IN_PROGRESS,
    FAILED,
    SUCCEEDED
  }
}

@Persisted
@Serializable
data class Repo(
    val id: RepoId,
    val name: RepoName,
    val owner: Username,
    val defaultBranch: BranchName,
)

@Persisted @Serializable @JvmInline value class RepoId(val value: Long)

/** E.g. `"githyb-actions-ci-dashboard"`. */
@Persisted @Serializable @JvmInline value class RepoName(val value: String)

@Persisted @Serializable @JvmInline value class BranchName(val value: String)

@Persisted
@Serializable
@JvmInline
value class Username(val value: String) {
  init {
    require(value.isNotBlank()) { "Username cannot be blank" }
  }
}

@Persisted @Serializable @JvmInline value class UserId(val value: Long)

@Persisted
@Serializable
data class User(val id: UserId, val username: Username, val avatarUrl: String)

@Persisted
@Serializable
data class Commit(
    val sha: String,
    val commitDate: Instant,
    val title: String,
    val message: String,
    val commiter: User
)
