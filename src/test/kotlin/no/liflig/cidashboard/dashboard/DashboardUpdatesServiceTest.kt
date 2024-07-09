package no.liflig.cidashboard.dashboard

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.liflig.cidashboard.persistence.CiStatusRepo
import org.junit.jupiter.api.Test
import test.util.Integration

@Integration
class DashboardUpdatesServiceTest {

  @Test
  fun `should get all ci statuses`() {
    // Given
    val repo = mockk<CiStatusRepo> { every { getAll() } returns emptyList() }
    val service = DashboardUpdatesService({ callback -> callback(repo) })
    // When
    service.handleDashboardUpdate("1")

    // Then
    verify { repo.getAll() }
  }
}
