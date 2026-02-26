package no.liflig.cidashboard.admin.auth

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
      domain: String,
      region: String,
      authBaseUrl: String,
      clientId: String,
      clientSecret: String,
      scopes: List<String>,
      callbackUri: Uri,
      persistence: OAuthPersistence,
      http: HttpHandler,
  ): OAuthProvider {
    log.info {
      field("cognito.domain", domain)
      field("cognito.region", region)
      "Creating Cognito OAuth provider"
    }

    val providerConfig =
        OAuthProviderConfig(
            authBase = Uri.of(authBaseUrl),
            authPath = "/oauth2/authorize",
            tokenPath = "/oauth2/token",
            credentials = Credentials(clientId, clientSecret),
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
