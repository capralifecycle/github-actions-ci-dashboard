package no.liflig.cidashboard.admin.config

import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.persistence.DashboardConfigRepo
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test

class GetDashboardConfigEndpointTest {

  @Test
  fun `should return all dashboard configs when no id is given`() {

    // Given
    val dashboards = List(3) { index -> DashboardConfig("$index", listOf()) }
    val configRepo = mockk<DashboardConfigRepo> { every { getAll() } returns dashboards }
    val service =
        DashboardConfigService(
            inTransaction = { callback -> callback(configRepo) },
            useDashboardConfigRepo = { callback -> callback(configRepo) },
        )
    val endpoint = GetDashboardConfigEndpoint(service)

    // When
    val response = endpoint(Request(Method.GET, "/admin/config"))

    // Then
    assertEquals(Status.OK, response.status)
    assertEquals(dashboards, DashboardConfig.bodyLensOfList(response))
  }

  @Test
  fun `should return a specific dashboard config when id is given`() {

    // Given
    val dashboards = List(3) { index -> DashboardConfig("$index", listOf()) }
    val configRepo = mockk<DashboardConfigRepo> { every { getAll() } returns dashboards }
    val service =
        DashboardConfigService(
            inTransaction = { callback -> callback(configRepo) },
            useDashboardConfigRepo = { callback -> callback(configRepo) },
        )
    val endpoint = GetDashboardConfigEndpoint(service)

    // When
    val response = endpoint(Request(Method.GET, "/admin/config").query("dashboardConfigId", "1"))

    // Then
    assertEquals(Status.OK, response.status)
    assertEquals(dashboards[1], DashboardConfig.bodyLens(response))
  }

  @Test
  fun `should return 404 when dashboard config id is not found`() {

    // Given
    val configRepo = mockk<DashboardConfigRepo> { every { getAll() } returns emptyList() }
    val service =
        DashboardConfigService(
            inTransaction = { callback -> callback(configRepo) },
            useDashboardConfigRepo = { callback -> callback(configRepo) },
        )
    val endpoint = GetDashboardConfigEndpoint(service)

    // When
    val response =
        endpoint(Request(Method.GET, "/admin/config").query("dashboardConfigId", "missing"))

    // Then
    assertEquals(Status.NOT_FOUND, response.status)
  }
}
