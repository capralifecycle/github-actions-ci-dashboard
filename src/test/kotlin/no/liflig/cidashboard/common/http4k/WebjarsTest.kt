package no.liflig.cidashboard.common.http4k

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WebjarsTest {
  @Test
  fun `should read htmx`() {
    // When
    val actualVersion = Webjars.htmxVersion

    // Then
    assertThat(actualVersion).startsWith("2") // Just bump this if htmx ever gets a major upgrade.
  }
}
