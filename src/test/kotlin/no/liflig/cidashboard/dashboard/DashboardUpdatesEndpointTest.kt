package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import no.liflig.cidashboard.common.http4k.MissingHelper
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.cidashboard.persistence.createCiStatus
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.junit.jupiter.api.Test

class DashboardUpdatesEndpointTest {

  @Test
  fun `should render empty dashboard list to say no builds`() {
    // Given
    val ciStatuses: List<CiStatus> = emptyList()
    val dashboardId = "1"
    val updatesService: DashboardUpdatesService = mockk {
      every { handleDashboardUpdate(dashboardId) } returns ciStatuses
    }

    val endpoint = DashboardUpdatesEndpoint(updatesService, useHotReload = false)

    val request =
        Request(Method.GET, "").with(DashboardUpdatesEndpoint.dashboardIdLens of dashboardId)

    // When
    val response = endpoint(request)

    // Then
    assertThat(response.bodyString()).isEqualTo("""  <span class="no-builds">No builds</span>""")
  }

  @Test
  fun `should render all dashboards from the service to html`() {
    // Given
    val ciStatuses: List<CiStatus> =
        listOf(
            createCiStatus(
                id = "1", repoName = "repo-a", lastStatus = CiStatus.PipelineStatus.SUCCEEDED),
            createCiStatus(
                id = "2", repoName = "repo-b", lastStatus = CiStatus.PipelineStatus.IN_PROGRESS),
            createCiStatus(
                id = "3", repoName = "repo-c", lastStatus = CiStatus.PipelineStatus.FAILED),
        )
    val dashboardId = "2"
    val updatesService: DashboardUpdatesService =
        mockk() { every { handleDashboardUpdate(dashboardId) } returns ciStatuses }

    val endpoint = DashboardUpdatesEndpoint(updatesService, useHotReload = false)

    val request =
        Request(Method.GET, "").with(DashboardUpdatesEndpoint.dashboardIdLens of dashboardId)

    // When
    val response = endpoint(request)
    val actualHtmlBody = response.bodyString()

    // Then
    assertThat(actualHtmlBody)
        .`as`("Invalid template variables. Make sure any inline class is not mangled in CiStatus")
        .doesNotContain(MissingHelper.ERROR_PREFIX)
        .`as`("Invalid rendered output")
        .isEqualTo(
            """  <div class="status status--SUCCEEDED">
    <span class="status__repo-name">repo-a</span>
    <span class="status__repo-branch">master</span>
    <span class="status__last-updated">2024-07-05T12:25:40Z</span>
    <span class="status__triggered-by">krissrex</span>
    <div class="commit status__commit">
      <span class="commit__title">Add feature</span>
      <span class="commit__sha">123abc</span>
      <div class="commiter commit__commiter">
        <img src="https://avatars.githubusercontent.com/u/7364831?v&#x3D;4" alt="Avatar" class="commiter__avatar">
        <span class="commiter__username">krissrex</span>
      </div>
    </div>
  </div>
  <div class="status status--IN_PROGRESS">
    <span class="status__repo-name">repo-b</span>
    <span class="status__repo-branch">master</span>
    <span class="status__last-updated">2024-07-05T12:25:40Z</span>
    <span class="status__triggered-by">krissrex</span>
    <div class="commit status__commit">
      <span class="commit__title">Add feature</span>
      <span class="commit__sha">123abc</span>
      <div class="commiter commit__commiter">
        <img src="https://avatars.githubusercontent.com/u/7364831?v&#x3D;4" alt="Avatar" class="commiter__avatar">
        <span class="commiter__username">krissrex</span>
      </div>
    </div>
  </div>
  <div class="status status--FAILED">
    <span class="status__repo-name">repo-c</span>
    <span class="status__repo-branch">master</span>
    <span class="status__last-updated">2024-07-05T12:25:40Z</span>
    <span class="status__triggered-by">krissrex</span>
    <div class="commit status__commit">
      <span class="commit__title">Add feature</span>
      <span class="commit__sha">123abc</span>
      <div class="commiter commit__commiter">
        <img src="https://avatars.githubusercontent.com/u/7364831?v&#x3D;4" alt="Avatar" class="commiter__avatar">
        <span class="commiter__username">krissrex</span>
      </div>
    </div>
  </div>
""")
  }
}
