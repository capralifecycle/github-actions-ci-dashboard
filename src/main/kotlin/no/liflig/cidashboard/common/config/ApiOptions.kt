package no.liflig.cidashboard.common.config

import java.util.Properties
import no.liflig.properties.booleanRequired
import no.liflig.properties.intRequired
import no.liflig.properties.string
import no.liflig.properties.stringNotEmpty
import org.http4k.core.Credentials
import org.http4k.filter.CorsPolicy

data class ApiOptions(
    val applicationName: String,
    /** Like `http://localhost`, `http://localhost:8080`, `https://myservice.prod.customer.com`. */
    val serverBaseUrl: String,
    val serverPort: Port,
    val corsPolicy: CorsPolicy,
    val openApiCredentials: Credentials,
    val logHttpBody: Boolean
) {

  companion object {
    fun from(props: Properties): ApiOptions =
        ApiOptions(
            applicationName = props.stringNotEmpty("service.name"),
            serverBaseUrl = props.stringNotEmpty("api.baseurl"),
            serverPort = Port(props.intRequired("server.port")),
            corsPolicy = CorsConfig.from(props).asPolicy(),
            openApiCredentials =
                Credentials(
                    user = props.stringNotEmpty("api.openapi.credentials.user"),
                    password = props.string("api.openapi.credentials.password") ?: "",
                ),
            logHttpBody = props.booleanRequired("log.http.body"))
  }
}

@JvmInline
value class Port(val value: Int) {
  init {
    require(value in 0..65535) { "Invalid port" }
  }
}
