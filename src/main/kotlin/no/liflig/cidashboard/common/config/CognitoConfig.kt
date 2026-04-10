package no.liflig.cidashboard.common.config

import java.util.Properties
import no.liflig.logging.getLogger
import no.liflig.properties.booleanRequired
import no.liflig.properties.stringNotEmpty
import no.liflig.properties.stringNotNull
import software.amazon.awssdk.regions.Region

data class CognitoConfig(
    val userPoolId: String,
    val clientId: String,
    val clientSecret: String,
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
    private val logger = getLogger()
    private const val DUMMY_VALUE = "not-configured"

    fun from(props: Properties): CognitoConfig? {
      if (!props.booleanRequired("admin.gui.enabled")) {
        return null
      }

      val requiredGroup = props.stringNotEmpty("cognito.requiredGroup")

      val bypassEnabled = props.booleanRequired("cognito.bypassEnabled")
      if (bypassEnabled) {
        logger.warn {
          "Cognito bypass enabled. Ensure this is not running in production: your admin gui is now unauthenticated."
        }
        return CognitoConfig(
            userPoolId = DUMMY_VALUE,
            clientId = DUMMY_VALUE,
            clientSecret = DUMMY_VALUE,
            domain = DUMMY_VALUE,
            region = Region.EU_WEST_1.id(),
            requiredGroup = requiredGroup,
            bypassEnabled = true,
            appBaseUrl = DUMMY_VALUE,
        )
      }

      return CognitoConfig(
          userPoolId = props.stringNotEmpty("cognito.userPoolId"),
          clientId = props.stringNotEmpty("cognito.clientId"),
          clientSecret = props.stringNotNull("cognito.clientSecret"),
          domain = props.stringNotEmpty("cognito.domain"),
          region = props.stringNotEmpty("cognito.region"),
          requiredGroup = requiredGroup,
          bypassEnabled = false,
          appBaseUrl = props.stringNotEmpty("cognito.appBaseUrl"),
      )
    }
  }
}
