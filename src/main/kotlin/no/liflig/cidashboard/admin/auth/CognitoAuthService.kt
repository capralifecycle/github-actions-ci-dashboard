package no.liflig.cidashboard.admin.auth

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
    private val config: CognitoConfig,
    httpClient: HttpHandler,
) {
  private val log = getLogger()
  private val jwtValidator = CognitoJwtValidator(config)
  private val persistence: OAuthPersistence = InsecureCookieBasedOAuthPersistence("cognito")

  private val callbackUri: Uri = Uri.of("${config.appBaseUrl}/admin/oauth/callback")

  private val oAuthProvider: OAuthProvider =
      CognitoOAuthProvider.create(
          config = config,
          http = httpClient,
          callbackUri = callbackUri,
          persistence = persistence,
          scopes = listOf("openid", "email", "profile"),
      )

  fun authFilter(): Filter = Filter { next ->
    { request ->
      if (config.bypassEnabled) {
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

      if (!user.groups.contains(config.requiredGroup)) {
        log.warn {
          field("user.username", user.username)
          field("user.groups", user.groups)
          field("required.group", config.requiredGroup)
          "User does not have required group"
        }
        return@Filter Response(Status.FORBIDDEN)
            .body("Access denied. Required group: ${config.requiredGroup}")
      }

      next(request.setUser(user))
    }
  }

  fun callbackHandler(): HttpHandler = oAuthProvider.callback

  private fun bypassUser(): CognitoUser =
      CognitoUser(
          username = "bypass-user",
          email = "bypass@localhost",
          groups = listOf(config.requiredGroup),
      )

  companion object {
    private val userKey: RequestLens<CognitoUser?> = RequestKey.optional("cognitoUser")

    internal fun Request.setUser(user: CognitoUser): Request = with(userKey of user)

    fun cognitoUser(request: Request): CognitoUser? = userKey(request)

    fun requireCognitoUser(request: Request): CognitoUser =
        cognitoUser(request) ?: throw IllegalStateException("No Cognito user in request context")
  }
}
