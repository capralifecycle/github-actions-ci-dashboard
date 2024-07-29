package no.liflig.cidashboard.admin.config

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verifySequence
import kotlin.test.Test
import no.liflig.cidashboard.DashboardConfig
import no.liflig.cidashboard.persistence.DashboardConfigRepo

class DashboardConfigServiceTest {

  @Test
  fun `should save all the dashboard configs when posted as list`() {

    // Given
    val configRepo = mockk<DashboardConfigRepo> { every { save(any()) } just Runs }
    val service = DashboardConfigService { callback -> callback(configRepo) }

    val dashboards = List(3) { index -> DashboardConfig("$index") }

    service.handleListUpdate(dashboards)

    // Then
    verifySequence {
      for (i in 0..2) {
        configRepo.save(dashboards[i])
      }
    }
  }
}
