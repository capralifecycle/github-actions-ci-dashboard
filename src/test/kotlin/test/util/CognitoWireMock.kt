package test.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.util.Date
import java.util.UUID

class CognitoWireMock(private val wireMock: WireMockServer) {
  private val rsaKey: RSAKey =
      RSAKeyGenerator(2048)
          .keyID(UUID.randomUUID().toString())
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.RS256)
          .generate()

  private val signer: JWSSigner = RSASSASigner(rsaKey)

  val jwksJson: String = JWKSet(listOf(rsaKey.toPublicJWK())).toString()

  val issuer: String
    get() = "${wireMock.baseUrl()}/cognito-idp/eu-north-1_test"

  val authBaseUrl: String
    get() = wireMock.baseUrl()

  val domain: String
    get() = wireMock.baseUrl().removePrefix("http://").removePrefix("https://")

  val region: String = "eu-north-1"

  val userPoolId: String = "eu-north-1_test"

  val clientId: String = "test-client-id"

  val clientSecret: String = "test-client-secret"

  fun setupStubs() {
    wireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/cognito-idp/eu-north-1_test/.well-known/jwks.json"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(jwksJson)
            )
    )

    wireMock.stubFor(
        WireMock.post(WireMock.urlPathEqualTo("/oauth2/token"))
            .willReturn(
                WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        """
                            {
                                "access_token": "${generateAccessToken()}",
                                "token_type": "Bearer",
                                "expires_in": 3600
                            }
                            """
                            .trimIndent()
                    )
            )
    )
  }

  fun generateAccessToken(
      username: String = "test-user",
      groups: List<String> = listOf("liflig-active"),
      expirationMinutes: Int = 60,
  ): String {
    val now = Date()
    val expiration = Date(now.time + expirationMinutes * 60 * 1000L)

    val claims =
        JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject(username)
            .audience(clientId)
            .issueTime(now)
            .expirationTime(expiration)
            .claim("cognito:username", username)
            .claim("email", "$username@example.com")
            .claim("cognito:groups", groups)
            .build()

    val signedJWT =
        SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.keyID).build(), claims)
    signedJWT.sign(signer)

    return signedJWT.serialize()
  }
}
