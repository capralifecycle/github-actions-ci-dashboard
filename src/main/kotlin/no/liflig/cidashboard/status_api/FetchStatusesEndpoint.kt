package no.liflig.cidashboard.status_api

import no.liflig.cidashboard.persistence.CiStatus
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Query
import org.http4k.lens.boolean
import org.http4k.lens.int

/**
 * Lets developers create tools to run on their local machines, such as
 * [XBar](https://xbarapp.com/), instead of using the dashboard.
 */
class FetchStatusesEndpoint(private val filteredStatusesService: FilteredStatusesService) :
    HttpHandler {

  companion object {
    private val repoFilterLens =
        Query.map { it.split(",").map { pattern -> Regex(pattern) } }
            .defaulted("repo_name", emptyList(), "Comma separated list of regex")
    private val userFilterLens =
        Query.map { it.split(",") }
            .defaulted("user_name", emptyList(), "Comma separated list of usernames to include")
    private val exceedCountToIncludeFailuresLens =
        Query.boolean()
            .defaulted(
                "exceed_count_to_include_failures",
                true,
                "Return more than <count> statuses to ensure all failures are included.",
            )
    private val countLens =
        Query.int().optional("count", "Max number of latest build statuses to fetch")

    private val bodyLens = Body.auto<List<CiStatus>>().toLens()
  }

  override fun invoke(request: Request): Response {
    val repoFilter: List<Regex> = repoFilterLens(request)
    val userFilter: List<String> = userFilterLens(request)
    val count: Int? = countLens(request)
    val includeAllFailures: Boolean = exceedCountToIncludeFailuresLens(request)

    val filteredCiStatuses =
        filteredStatusesService.getFilteredCiStatuses(
            repoFilter,
            userFilter,
            count,
            includeAllFailures,
        )

    return Response(Status.OK).with(bodyLens of filteredCiStatuses)
  }
}
