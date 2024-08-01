package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.DashboardConfigId
import no.liflig.cidashboard.common.config.ClientSecretToken
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
    val ciStatuses = DashboardData(emptyList(), emptyList())
    val dashboardId = DashboardConfigId("1")
    val updatesService: DashboardUpdatesService = mockk {
      every { getUpdatedDashboardData(dashboardId) } returns ciStatuses
    }

    val secretToken = "my-secret-token"
    val endpoint =
        DashboardUpdatesEndpoint(
            updatesService, secretToken = ClientSecretToken(secretToken), useHotReload = false)

    val request =
        Request(Method.GET, "")
            .with(DashboardConfigId.queryLens of dashboardId)
            .with(DashboardUpdatesEndpoint.tokenLens of secretToken)

    // When
    val response = endpoint(request)

    // Then
    verifyStringSnapshot("dashboard-updates-endpoint/empty-list.html", response.bodyString())
  }

  @Test
  fun `should render all ci statuses from the service to html`() {
    // Given
    val ciStatuses =
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
    val dashboardId = DashboardConfigId("default")
    val updatesService: DashboardUpdatesService =
        mockk() { every { getUpdatedDashboardData(dashboardId) } returns ciStatuses }

    val secretToken = "my-secret-token"
    val endpoint =
        DashboardUpdatesEndpoint(
            updatesService, secretToken = ClientSecretToken(secretToken), useHotReload = false)

    val request =
        Request(Method.GET, "")
            .with(DashboardConfigId.queryLens of dashboardId)
            .with(DashboardUpdatesEndpoint.tokenLens of secretToken)

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

  @Test
  fun `should render all ci statuses form service to html with config`() {
    // Given
    val dashboardId = DashboardConfigId("custom-config")
    val ciStatuses =
        DashboardData(
            config = DashboardConfig(dashboardId, displayName = "display-name"),
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

    val updatesService: DashboardUpdatesService =
        mockk() { every { getUpdatedDashboardData(dashboardId) } returns ciStatuses }

    val secretToken = "my-secret-token"
    val endpoint =
        DashboardUpdatesEndpoint(
            updatesService, secretToken = ClientSecretToken(secretToken), useHotReload = false)

    val request =
        Request(Method.GET, "")
            .with(DashboardConfigId.queryLens of dashboardId)
            .with(DashboardUpdatesEndpoint.tokenLens of secretToken)

    // When
    val response = endpoint(request)
    val actualHtmlBody = response.bodyString()

    // Then
    assertThat(actualHtmlBody)
        .`as`("Invalid template variables. Make sure any inline class is not mangled in CiStatus")
        .doesNotContain(MissingHelper.ERROR_PREFIX)
        .`as`("Invalid rendered output")

    verifyStringSnapshot("dashboard-updates-endpoint/updates-with-config.html", actualHtmlBody)
  }
}
