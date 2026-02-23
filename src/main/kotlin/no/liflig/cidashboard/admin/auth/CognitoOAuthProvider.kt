package no.liflig.cidashboard.admin.auth

import no.liflig.cidashboard.common.config.CognitoConfig
import no.liflig.logging.getLogger
import org.http4k.core.Credentials
import org.http4k.core.HttpHandler
import org.http4k.core.Uri
import org.http4k.security.OAuthPersistence
import org.http4k.security.OAuthProvider
import org.http4k.security.OAuthProviderConfig

object CognitoOAuthProvider {
  private val log = getLogger()

  fun create(
      config: CognitoConfig,
      http: HttpHandler,
      callbackUri: Uri,
      persistence: OAuthPersistence,
      scopes: List<String> = listOf("openid", "email", "profile"),
  ): OAuthProvider {
    log.info {
      field("cognito.domain", config.domain)
      field("cognito.region", config.region)
      "Creating Cognito OAuth provider"
    }

    val providerConfig =
        OAuthProviderConfig(
            authBase = Uri.of(config.authBaseUrl),
            authPath = "/oauth2/authorize",
            tokenPath = "/oauth2/token",
            credentials = Credentials(config.clientId, config.clientSecret),
        )

    return OAuthProvider(
        providerConfig,
        http,
        callbackUri,
        scopes,
        persistence,
    )
  }
}
