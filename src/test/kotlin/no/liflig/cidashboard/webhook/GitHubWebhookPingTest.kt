package no.liflig.cidashboard.webhook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GitHubWebhookPingTest {

  @Test
  fun `should deserialize from json`() {
    // Given
    val pingEventJson = javaClass.classLoader.getResource("acceptancetests/webhook/github-ping-body.json")!!.readText()

    // When
    val event = GitHubWebhookPing.fromJson(pingEventJson)

    // Then
    assertThat(event.hook.id).isEqualTo(488374259)
  }
}
