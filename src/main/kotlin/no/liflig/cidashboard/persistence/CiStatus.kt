@file:UseSerializers(InstantSerializer::class)

package no.liflig.cidashboard.persistence

import java.time.Instant
import kotlin.time.Duration
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.common.serialization.InstantSerializer

/**
 * The main object for GitHub Actions CI status. This is saved to the database with [CiStatusRepo].
 */
@Persisted
@Serializable
data class CiStatus(
    /** Primary key */
    @get:JvmName("getId") val id: CiStatusId,
    val repo: Repo,
    /*A bit hacky, but all `value class` needs a JvmName to stop kotlin from mangling it.
    Otherwise, Handlebars can't find the getter because it is named e.g. `getBranch-0F3G-xM`
    https://kotlinlang.org/docs/inline-classes.html#mangling
    */
    @get:JvmName("getBranch") val branch: BranchName,
    val lastStatus: PipelineStatus,
    val startedAt: Instant,
    /** The timestamp of the workflow event that updated this [lastStatus]. */
    val lastUpdatedAt: Instant,
    /**
     * Most CI tools count how many times they have run, and increment a counter by 1. This gives
     * you an ordering of builds. It is useful for us because we can ignore data from older builds.
     */
    val buildNumber: Long,
    val lastCommit: Commit,
    @get:JvmName("getTriggeredBy") val triggeredBy: Username,
    val lastSuccessfulCommit: Commit? = null,
    @get:JvmName("getDurationOfLastSuccess") val durationOfLastSuccess: Duration? = null,
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

/** Primary key and ID for a CI status. Should be unique per repo, branch and workflow. */
@Serializable @Persisted @JvmInline value class CiStatusId(val value: String)

@Persisted
@Serializable
data class Repo(
    @get:JvmName("getId") val id: RepoId,
    @get:JvmName("getName") val name: RepoName,
    @get:JvmName("getOwner") val owner: Username,
    @get:JvmName("getDefaultBranch") val defaultBranch: BranchName,
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
data class User(
    @get:JvmName("getId") val id: UserId,
    @get:JvmName("getUsername") val username: Username,
    val avatarUrl: String
)

@Persisted
@Serializable
data class Commit(
    val sha: String,
    val commitDate: Instant,
    val title: String,
    val message: String,
    val commiter: User
)
