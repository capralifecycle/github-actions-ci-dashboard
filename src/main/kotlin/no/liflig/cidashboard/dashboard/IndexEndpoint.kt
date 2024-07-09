package no.liflig.cidashboard.dashboard

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.ViewModel
import org.http4k.template.viewModel

/** HTTP handler for the main dashboard webpage, like `"/"` or `"/index.html"`. */
class IndexEndpoint(useHotReload: Boolean) : HttpHandler {

  companion object {
    private val templateDir = "handlebars-htmx-templates"
  }

  private val renderer =
      if (useHotReload) {
        HandlebarsTemplates().HotReload("src/main/resources/$templateDir")
      } else {
        HandlebarsTemplates().CachingClasspath(templateDir)
      }
  private val bodyLens = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()

  override fun invoke(request: Request): Response {
    // The renderer uses the ViewModel class to identify which template to use.
    return Response(Status.OK).with(bodyLens of Index("1", "abc", "/dashboard-updates"))
  }
}

data class Index(
    val dashboardId: String,
    val secretToken: String,
    val pollUrl: String,
    val pollRateSeconds: Long = 5,
    val version: String = LATEST_VERSION
) : ViewModel {
  override fun template(): String = "index"

  companion object {
    /** On breaking changes, bumping this will force the client to refresh the entire page. */
    const val LATEST_VERSION = "1"
  }
}
