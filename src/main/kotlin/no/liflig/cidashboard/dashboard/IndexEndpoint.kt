package no.liflig.cidashboard.dashboard

import kotlin.time.Duration
import kotlin.time.DurationUnit
import no.liflig.cidashboard.common.config.ClientSecretToken
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.Query
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

/** HTTP handler for the main dashboard webpage, like `"/"` or `"/index.html"`. */
class IndexEndpoint(
    private val secretToken: ClientSecretToken,
    useHotReload: Boolean,
    private val updatesPollRate: Duration
) : HttpHandler {

  companion object {
    val tokenLens =
        Query.required(
            "token", "Authorization so strangers don't see our repositories and thus customers.")
  }

  private val renderer =
      if (useHotReload) {
        Renderer.hotReloading
      } else {
        Renderer.classpath
      }
  private val bodyLens = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()

  override fun invoke(request: Request): Response {
    val actualToken = tokenLens(request)
    if (actualToken != secretToken.value) {
      return Response(Status.FORBIDDEN).body("Invalid token in query")
    }

    // The renderer uses the ViewModel class to identify which template to use.
    return Response(Status.OK)
        .with(
            bodyLens of
                Index(
                    // FIXME: dont use placeholders; read from request.
                    "1",
                    actualToken,
                    "/dashboard-updates",
                    pollRateSeconds = updatesPollRate.toDouble(DurationUnit.SECONDS)))
  }
}

data class Index(
    val dashboardId: String,
    val secretToken: String,
    val pollUrl: String,
    val pollRateSeconds: Double = 5.0,
    val version: String = LATEST_VERSION
) : ViewModel {
  override fun template(): String = "index"

  companion object {
    /** On breaking changes, bumping this will force the client to refresh the entire page. */
    const val LATEST_VERSION = "7"
  }
}
