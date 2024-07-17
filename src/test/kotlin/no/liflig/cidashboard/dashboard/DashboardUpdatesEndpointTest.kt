package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.createCiStatus
import no.liflig.snapshot.verifyStringSnapshot
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.junit.jupiter.api.Test

class DashboardUpdatesEndpointTest {

  @Test
  fun `should render empty list to say no builds`() {
    // Given
    val ciStatuses: DashboardData = DashboardData(emptyList(), emptyList())
    val dashboardId = "1"
    val updatesService: DashboardUpdatesService = mockk {
      every { getUpdatedDashboardData(dashboardId) } returns ciStatuses
    }

    val endpoint = DashboardUpdatesEndpoint(updatesService, useHotReload = false)

    val request =
        Request(Method.GET, "")
            .with(DashboardUpdatesEndpoint.dashboardIdLens of dashboardId)
            .with(DashboardUpdatesEndpoint.tokenLens of "todo-add-token-via-config-api")

    // When
    val response = endpoint(request)

    // Then
    assertThat(response.bodyString())
        .isEqualTo(
            """<div id="statuses" class="statuses">
              |    <span class="no-builds">No builds</span>
              |</div>"""
                .trimMargin())
  }

  @Test
  fun `should render all ci statuses from the service to html`() {
    // Given
    val ciStatuses: DashboardData =
        DashboardData(
            lastBuilds =
                listOf(
                    createCiStatus(
                        id = "1",
                        repoName = "repo-a",
                        lastStatus = CiStatus.PipelineStatus.SUCCEEDED),
                    createCiStatus(
                        id = "2",
                        repoName = "repo-b",
                        lastStatus = CiStatus.PipelineStatus.IN_PROGRESS),
                    createCiStatus(
                        id = "3", repoName = "repo-c", lastStatus = CiStatus.PipelineStatus.FAILED),
                ),
            allFailedBuilds =
                listOf(
                    createCiStatus(
                        id = "3", repoName = "repo-c", lastStatus = CiStatus.PipelineStatus.FAILED),
                ))
    val dashboardId = "2"
    val updatesService: DashboardUpdatesService =
        mockk() { every { getUpdatedDashboardData(dashboardId) } returns ciStatuses }

    val endpoint = DashboardUpdatesEndpoint(updatesService, useHotReload = false)

    val request =
        Request(Method.GET, "")
            .with(DashboardUpdatesEndpoint.dashboardIdLens of dashboardId)
            .with(DashboardUpdatesEndpoint.tokenLens of "todo-add-token-via-config-api")

    // When
    val response = endpoint(request)
    val actualHtmlBody = response.bodyString()

    // Then
    assertThat(actualHtmlBody)
        .`as`("Invalid template variables. Make sure any inline class is not mangled in CiStatus")
        .doesNotContain(MissingHelper.ERROR_PREFIX)
        .`as`("Invalid rendered output")

    verifyStringSnapshot("dashboard-updates-endpoint/updates.html", actualHtmlBody)
  }
}
