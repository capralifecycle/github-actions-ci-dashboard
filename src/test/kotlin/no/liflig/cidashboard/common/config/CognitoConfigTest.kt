package no.liflig.cidashboard.common.config

import java.util.Properties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CognitoConfigTest {

  @Test
  fun `should create config from properties`() {
    val props =
        Properties().apply {
          setProperty("admin.gui.enabled", "true")
          setProperty("cognito.userPoolId", "eu-north-1_abc123")
          setProperty("cognito.clientId", "client-id-123")
          setProperty("cognito.clientSecret", "secret-123")
          setProperty("cognito.domain", "my-app")
          setProperty("cognito.region", "eu-north-1")
          setProperty("cognito.requiredGroup", "admin-group")
          setProperty("cognito.appBaseUrl", "https://myapp.example.com")
          setProperty("cognito.bypassEnabled", "false")
        }

    val config = CognitoConfig.from(props)

    assertThat(config).isNotNull
    assertThat(config!!.userPoolId).isEqualTo("eu-north-1_abc123")
    assertThat(config.clientId).isEqualTo("client-id-123")
    assertThat(config.clientSecret).isEqualTo("secret-123")
    assertThat(config.domain).isEqualTo("my-app")
    assertThat(config.region).isEqualTo("eu-north-1")
    assertThat(config.requiredGroup).isEqualTo("admin-group")
    assertThat(config.appBaseUrl).isEqualTo("https://myapp.example.com")
    assertThat(config.bypassEnabled).isFalse
  }

  @Test
  fun `should throw when required properties are missing and bypass is disabled`() {
    val props =
        Properties().apply {
          setProperty("admin.gui.enabled", "true")
          setProperty("cognito.bypassEnabled", "false")
        }

    assertThrows<IllegalArgumentException> {
      val config = CognitoConfig.from(props)
    }
  }

  @Test
  fun `should create config with dummy values when bypass is enabled but properties missing`() {
    val props =
        Properties().apply {
          setProperty("admin.gui.enabled", "true")
          setProperty("cognito.bypassEnabled", "true")
          setProperty("cognito.requiredGroup", "test")
        }

    val config = CognitoConfig.from(props)

    assertThat(config).isNotNull
    assertThat(config!!.userPoolId).isEqualTo("not-configured")
    assertThat(config.clientId).isEqualTo("not-configured")
    assertThat(config.domain).isEqualTo("not-configured")
    assertThat(config.region).isEqualTo("eu-west-1")
    assertThat(config.appBaseUrl).isEqualTo("not-configured")
    assertThat(config.bypassEnabled).isTrue
  }

  @Test
  fun `should generate correct issuer url`() {
    val config =
        CognitoConfig(
            userPoolId = "eu-north-1_abc123",
            clientId = "client-id",
            clientSecret = "123",
            domain = "my-app",
            region = "eu-north-1",
            requiredGroup = "admin",
            bypassEnabled = false,
            appBaseUrl = "https://myapp.example.com",
        )

    assertThat(config.issuerUrl)
        .isEqualTo("https://cognito-idp.eu-north-1.amazonaws.com/eu-north-1_abc123")
  }

  @Test
  fun `should generate correct jwks url`() {
    val config =
        CognitoConfig(
            userPoolId = "eu-north-1_abc123",
            clientId = "client-id",
            clientSecret = "123",
            domain = "my-app",
            region = "eu-north-1",
            requiredGroup = "admin",
            bypassEnabled = false,
            appBaseUrl = "https://myapp.example.com",
        )

    assertThat(config.jwksUrl)
        .isEqualTo(
            "https://cognito-idp.eu-north-1.amazonaws.com/eu-north-1_abc123/.well-known/jwks.json"
        )
  }

  @Test
  fun `should generate correct oauth endpoints`() {
    val config =
        CognitoConfig(
            userPoolId = "eu-north-1_abc123",
            clientId = "client-id",
            clientSecret = "123",
            domain = "my-app",
            region = "eu-north-1",
            requiredGroup = "admin",
            bypassEnabled = false,
            appBaseUrl = "https://myapp.example.com",
        )

    assertThat(config.authorizationEndpoint)
        .isEqualTo("https://my-app.auth.eu-north-1.amazoncognito.com/oauth2/authorize")
    assertThat(config.tokenEndpoint)
        .isEqualTo("https://my-app.auth.eu-north-1.amazoncognito.com/oauth2/token")
    assertThat(config.logoutEndpoint)
        .isEqualTo("https://my-app.auth.eu-north-1.amazoncognito.com/logout")
  }
}
