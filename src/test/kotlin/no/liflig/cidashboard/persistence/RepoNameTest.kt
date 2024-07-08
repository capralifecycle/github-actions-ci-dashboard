package no.liflig.cidashboard.persistence

import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RepoNameTest {

  @Test
  fun `should serialize to json`() {
    // Given
    val name = RepoName("test-repo")

    // When
    val json = Json.encodeToString(RepoName.serializer(), name)

    // Then
    assertThat(json).isEqualTo("\"test-repo\"")
  }

  @Test
  fun `should deserialize from json`() {
    // Given
    val json = "\"test-repo\""

    // When
    val result = Json.decodeFromString(RepoName.serializer(), json)

    // Then
    assertThat(result.value).isEqualTo("test-repo")
  }
}
