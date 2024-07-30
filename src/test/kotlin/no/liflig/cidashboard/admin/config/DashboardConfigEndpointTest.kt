package no.liflig.cidashboard.admin.config

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.persistence.DashboardConfigRepo
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.with
import org.junit.jupiter.api.Test

class DashboardConfigEndpointTest {

  @Test
  fun `should handle config list update`() {

    // Given
    val configRepo = mockk<DashboardConfigRepo> { every { save(any()) } just Runs }
    val service = DashboardConfigService { callback -> callback(configRepo) }
    val endpoint = DashboardConfigEndpoint(service)

    val dashboards = List(3) { index -> DashboardConfig("$index", listOf()) }

    val request = Request(Method.POST, "").with(DashboardConfig.bodyLensOfList of dashboards)

    // Then
    endpoint(request)

    verify { service.handleListUpdate(dashboards) }
  }
}
