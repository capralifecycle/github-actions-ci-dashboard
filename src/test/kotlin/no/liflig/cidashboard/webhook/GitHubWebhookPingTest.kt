package no.liflig.cidashboard.webhook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import test.util.loadResource

class GitHubWebhookPingTest {

  @Test
  fun `should deserialize from json`() {
    // Given
    val pingEventJson = loadResource("acceptancetests/webhook/github-ping-body.json")

    // When
    val event = GitHubWebhookPing.fromJson(pingEventJson)

    // Then
    assertThat(event.hook.id).isEqualTo(488374259)
  }
}
