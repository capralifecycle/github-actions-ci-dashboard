package no.liflig.cidashboard

import no.liflig.snapshot.verifyJsonSnapshot
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import test.util.loadResource

class DashboardConfigTest {

  @Test
  fun `should serialize and deserialize to json`() {
    assertDoesNotThrow {
      val json = DashboardConfig.fromJson(loadResource("json/dashboardconfig.json")).toJson()
      verifyJsonSnapshot("persistence/dashboardconfig.json", json)
    }
  }
}
