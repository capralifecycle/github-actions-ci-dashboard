package no.liflig.cidashboard.webhook

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.common.serialization.InstantSerializer
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

/** This is the main event we care about from GitHub Actions. */
@Serializable
data class GitHubWebhookWorkflowRun(
    val action: Action,
    @SerialName("workflow_run") val workflowRun: WorkflowRun,
    val workflow: Workflow,
    val repository: Repository,
    val organization: Organization?,
    val sender: Sender,
) : WebhookPayload {

  companion object {
    val bodyLens: BiDiBodyLens<GitHubWebhookWorkflowRun> =
        Body.auto<GitHubWebhookWorkflowRun>().toLens()

    private val json = Json { ignoreUnknownKeys = true }

    fun fromJson(jsonString: String): GitHubWebhookWorkflowRun = json.decodeFromString(jsonString)
  }

  @Serializable
  enum class Action {
    @SerialName("requested") Requested,
    @SerialName("in_progress") InProgress,
    @SerialName("completed") Completed,
  }

  @Serializable
  data class WorkflowRun(
      /** `9808246639` */
      val id: Long,

      /** `"ci"` */
      val name: String?,
      /** `"master"` */
      @SerialName("head_branch") val headBranch: String,
      /** `"a7facd9ed12aeb1857815c3d85a629cdbd306218"` */
      @SerialName("head_sha") val headSha: String,

      /** The commit message title. `"Add sample code for processing webhooks"` */
      @SerialName("display_title") val displayTitle: String,

      /** `29` */
      @SerialName("run_number") val runNumber: Long,
      val status: Status,

      /** Null on [Action.Requested] and [Action.InProgress] */
      val conclusion: Conclusion?,

      /** `"2024-07-05T12:23:52Z"` */
      @SerialName("updated_at")
      @Serializable(with = InstantSerializer::class)
      val updatedAt: Instant,

      /** `"2024-07-05T12:23:52Z"` */
      @SerialName("run_started_at")
      @Serializable(with = InstantSerializer::class)
      val runStartedAt: Instant,
      val actor: Actor,
      @SerialName("triggering_actor") val triggeringActor: Actor,
      @SerialName("head_commit") val headCommit: HeadCommit,
  ) {

    @Serializable
    enum class Status {
      @SerialName("requested") Requested,
      @SerialName("in_progress") InProgress,
      @SerialName("completed") Completed,
      @SerialName("queued") Queued,
      @SerialName("pending") Pending,
      @SerialName("waiting") Waiting,
    }

    @Serializable
    enum class Conclusion {
      @SerialName("action_required") ActionRequired,
      @SerialName("cancelled") Cancelled,
      @SerialName("failure") Failure,
      @SerialName("neutral") Neutral,
      @SerialName("skipped") Skipped,
      @SerialName("stale") Stale,
      @SerialName("success") Success,
      @SerialName("timed_out") TimedOut,
    }

    @Serializable
    data class Actor(
        /** `"krissrex"` */
        val login: String,
        /** `7364831` */
        val id: Long,
        /** `"https://avatars.githubusercontent.com/u/7364831?v=4"` */
        @SerialName("avatar_url") val avatarUrl: String,
        val type: Sender.Type,
    )

    @Serializable
    data class HeadCommit(
        /** Git sha. `"a7facd9ed12aeb1857815c3d85a629cdbd306218"` */
        val id: String,
        /** Git commit message. `"Add sample code for processing webhooks\n\nCALS-816"` */
        val message: String,
        /** `"2024-07-05T12:23:47Z"`. */
        @Serializable(with = InstantSerializer::class) val timestamp: Instant,
        val author: Committer,
        val committer: Committer,
    ) {

      @Serializable
      data class Committer(
          /** `"Kari Nordmann"` */
          val name: String,

          // /** `"kristian@example.com"` */
          // val email: String?,
      )
    }
  }

  @Serializable
  data class Workflow(
      /**
       * The ID of the workflow.
       *
       * <p>
       * Note that this is shared across branches in a repo. That means a run in `master` and run in
       * `feat/make-change` will have the *same* workflow id.
       *
       * E.g. `105563496`
       */
      val id: Long,
      /** `"ci"` */
      val name: String,
      /** `".github/workflows/ci.yaml"` */
      val path: String,
  )

  @Serializable
  data class Repository(
      /** Unique id for the repository. `823587546`. */
      val id: Long,
      /** `"capralifecycle/github-actions-ci-dashboard"` */
      @SerialName("full_name") val fullName: String,
      /** `"github-actions-ci-dashboard"` */
      val private: Boolean,
      val owner: Owner,
      val name: String,
      /** `"master"`, `"main"`. */
      @SerialName("default_branch") val defaultBranch: String,
      val archived: Boolean,
      val topics: List<String> = emptyList(),
  ) {

    @Serializable
    data class Owner(
        /** `"capralifecycle"` */
        val login: String,
        /** `13219542` */
        val id: Long,
        /** `"https://avatars.githubusercontent.com/u/13219542?v=4"` */
        @SerialName("avatar_url") val avatarUrl: String,
    )
  }

  @Serializable
  data class Organization(
      /** `"capralifecycle"` */
      val login: String
  )

  @Serializable
  data class Sender(
      /** `"renovate[bot]"`, `"krissrex"`. */
      val login: String,
      /** `"https://avatars.githubusercontent.com/u/7364831?v=4"` */
      @SerialName("avatar_url") val avatarUrl: String,
      val type: Type,
  ) {

    @Serializable
    enum class Type {
      User,
      Bot,
    }
  }
}
