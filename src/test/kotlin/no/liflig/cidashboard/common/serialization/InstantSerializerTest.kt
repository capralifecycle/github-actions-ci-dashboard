package no.liflig.cidashboard.common.serialization

import java.time.Instant
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class InstantSerializerTest {

  @Test
  internal fun `should serialize to ISO timestamp`() {
    // Given
    val instant = Instant.parse("2022-06-23T11:58:47.958123Z")

    // When
    val actual = Json.encodeToString(InstantSerializer, instant)

    // Then
    assertThat(actual).isEqualTo("\"2022-06-23T11:58:47.958123Z\"")
  }
}
