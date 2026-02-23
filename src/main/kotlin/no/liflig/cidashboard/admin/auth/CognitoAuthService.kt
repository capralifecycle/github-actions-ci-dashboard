package no.liflig.cidashboard.admin.auth

import java.net.URI
import no.liflig.cidashboard.common.config.CognitoConfig
import no.liflig.logging.getLogger
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.RequestKey
import org.http4k.lens.RequestLens
import org.http4k.security.InsecureCookieBasedOAuthPersistence
import org.http4k.security.OAuthPersistence
import org.http4k.security.OAuthProvider

class CognitoAuthService(
    config: CognitoConfig,
    httpClient: HttpHandler,
) {
  private val log = getLogger()

  private val bypassEnabled: Boolean = config.bypassEnabled
  private val requiredGroup: String = config.requiredGroup

  private val jwtValidator =
      CognitoJwtValidator(
          jwksUrl = URI(config.jwksUrl).toURL(),
          clientId = config.clientId,
          issuerUrl = config.issuerUrl,
      )
  private val persistence: OAuthPersistence = InsecureCookieBasedOAuthPersistence("cognito")

  private val callbackUri: Uri = Uri.of("${config.appBaseUrl}/admin/oauth/callback")
  private val oAuthProvider: OAuthProvider =
      CognitoOAuthProvider.create(
          domain = config.domain,
          region = config.region,
          authBaseUrl = config.authBaseUrl,
          clientId = config.clientId,
          clientSecret = config.clientSecret,
          scopes = listOf("openid", "email", "profile"),
          callbackUri = callbackUri,
          persistence = persistence,
          http = httpClient,
      )

  fun authFilter(): Filter = Filter { next ->
    { request ->
      if (bypassEnabled) {
        log.debug { "Cognito auth bypass enabled - skipping authentication" }
        return@Filter next(request.setUser(bypassUser()))
      }

      val accessToken = persistence.retrieveToken(request)

      if (accessToken == null) {
        log.debug { "No access token found - delegating to OAuth provider for redirect" }
        return@Filter oAuthProvider.authFilter.then(next)(request)
      }

      val user = jwtValidator.validate(accessToken.value)

      if (user == null) {
        log.debug { "Invalid access token - delegating to OAuth provider for redirect" }
        return@Filter oAuthProvider.authFilter.then(next)(request)
      }

      if (!user.groups.contains(requiredGroup)) {
        log.warn {
          field("user.username", user.username)
          field("user.groups", user.groups)
          field("required.group", requiredGroup)
          "User does not have required group"
        }
        return@Filter Response(Status.FORBIDDEN)
            .body("Access denied. Required group: $requiredGroup")
      }

      next(request.setUser(user))
    }
  }

  fun callbackHandler(): HttpHandler = oAuthProvider.callback

  private fun bypassUser(): CognitoUser =
      CognitoUser(
          username = "bypass-user",
          email = "bypass@localhost",
          groups = listOf(requiredGroup),
      )

  companion object {
    private val userKey: RequestLens<CognitoUser?> = RequestKey.optional("cognitoUser")

    internal fun Request.setUser(user: CognitoUser): Request = with(userKey of user)

    fun cognitoUser(request: Request): CognitoUser? = userKey(request)

    fun requireCognitoUser(request: Request): CognitoUser =
        cognitoUser(request) ?: throw IllegalStateException("No Cognito user in request context")
  }
}
