package no.liflig.cidashboard.common.config

import java.util.Properties
import no.liflig.properties.string

data class CognitoConfig(
    val userPoolId: String,
    val clientId: String,
    val clientSecret: String?,
    val domain: String,
    val region: String,
    val requiredGroup: String,
    val bypassEnabled: Boolean,
    val issuerUrlOverride: String? = null,
    val authBaseOverride: String? = null,
    val appBaseUrl: String,
) {
  val issuerUrl: String
    get() = issuerUrlOverride ?: "https://cognito-idp.$region.amazonaws.com/$userPoolId"

  val jwksUrl: String
    get() = "$issuerUrl/.well-known/jwks.json"

  val authBaseUrl: String
    get() = authBaseOverride ?: "https://$domain.auth.$region.amazoncognito.com"

  val authorizationEndpoint: String
    get() = "$authBaseUrl/oauth2/authorize"

  val tokenEndpoint: String
    get() = "$authBaseUrl/oauth2/token"

  val logoutEndpoint: String
    get() = "$authBaseUrl/logout"

  companion object {
    private const val DUMMY_VALUE = "not-configured"

    fun from(props: Properties): CognitoConfig? {
      val bypassEnabled = props.string("cognito.bypassEnabled")?.toBoolean() ?: false

      val userPoolId =
          props.string("cognito.userPoolId")?.takeIf { it.isNotBlank() }
              ?: if (bypassEnabled) DUMMY_VALUE else return null
      val clientId =
          props.string("cognito.clientId")?.takeIf { it.isNotBlank() }
              ?: if (bypassEnabled) DUMMY_VALUE else return null
      val domain =
          props.string("cognito.domain")?.takeIf { it.isNotBlank() }
              ?: if (bypassEnabled) DUMMY_VALUE else return null
      val region =
          props.string("cognito.region")?.takeIf { it.isNotBlank() }
              ?: if (bypassEnabled) DUMMY_VALUE else return null
      val appBaseUrl =
          props.string("cognito.appBaseUrl")?.takeIf { it.isNotBlank() }
              ?: if (bypassEnabled) DUMMY_VALUE else return null

      return CognitoConfig(
          userPoolId = userPoolId,
          clientId = clientId,
          clientSecret = props.string("cognito.clientSecret")?.takeIf { it.isNotBlank() },
          domain = domain,
          region = region,
          requiredGroup =
              props.string("cognito.requiredGroup")?.takeIf { it.isNotBlank() } ?: "liflig-active",
          bypassEnabled = bypassEnabled,
          appBaseUrl = appBaseUrl,
      )
    }
  }
}
