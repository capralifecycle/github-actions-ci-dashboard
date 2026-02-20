package no.liflig.cidashboard.admin.auth

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import java.net.URL
import no.liflig.cidashboard.common.config.CognitoConfig
import no.liflig.logging.getLogger

data class CognitoUser(
    val username: String,
    val email: String?,
    val groups: List<String>,
)

class CognitoJwtValidator(private val config: CognitoConfig) {
  private val log = getLogger()

  private val jwtProcessor: ConfigurableJWTProcessor<SecurityContext> =
      DefaultJWTProcessor<SecurityContext>().apply {
        val jwkSetUrl = URL(config.jwksUrl)
        val jwkSource = JWKSourceBuilder.create<SecurityContext>(jwkSetUrl).build()
        val keySelector = JWSVerificationKeySelector(JWSAlgorithm.RS256, jwkSource)
        setJWSKeySelector(keySelector)
      }

  fun validate(token: String): CognitoUser? {
    return try {
      val claims = jwtProcessor.process(token, null)
      validateClaims(claims)?.let { user ->
        log.debug {
          field("cognito.username", user.username)
          field("cognito.groups", user.groups)
          "Validated Cognito JWT"
        }
        user
      }
    } catch (e: Exception) {
      log.warn(e) { "Failed to validate Cognito JWT" }
      null
    }
  }

  private fun validateClaims(claims: JWTClaimsSet): CognitoUser? {
    val issuer = claims.issuer
    if (issuer != config.issuerUrl) {
      log.warn {
        field("expected.issuer", config.issuerUrl)
        field("actual.issuer", issuer)
        "JWT issuer mismatch"
      }
      return null
    }

    val audience = claims.audience
    if (!audience.contains(config.clientId)) {
      log.warn {
        field("expected.audience", config.clientId)
        field("actual.audience", audience)
        "JWT audience mismatch"
      }
      return null
    }

    val expirationTime = claims.expirationTime
    if (expirationTime != null && expirationTime.before(java.util.Date())) {
      log.warn { "JWT has expired" }
      return null
    }

    val username = claims.getClaim("cognito:username") as? String ?: claims.subject
    val email = claims.getClaim("email") as? String

    @Suppress("UNCHECKED_CAST")
    val groups =
        (claims.getClaim("cognito:groups") as? List<String>)
            ?: (claims.getClaim("custom:groups") as? List<String>)
            ?: emptyList()

    return CognitoUser(username = username, email = email, groups = groups)
  }
}
