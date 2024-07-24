package no.liflig.cidashboard.common.config

import java.util.Properties
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import no.liflig.properties.boolean
import no.liflig.properties.booleanRequired
import no.liflig.properties.intRequired
import no.liflig.properties.long
import no.liflig.properties.stringNotEmpty
import org.http4k.filter.CorsPolicy

data class ApiOptions(
    val applicationName: String,
    val serverPort: Port,
    val corsPolicy: CorsPolicy,
    val logHttpBody: Boolean,
    val hotReloadTemplates: Boolean,
    /**
     * How fast a client should poll the
     * [UpdatesEndpoint][no.liflig.cidashboard.dashboard.DashboardUpdatesEndpoint]
     */
    val updatesPollRate: Duration,
    val clientSecretToken: ClientSecretToken
) {

  companion object {
    fun from(props: Properties): ApiOptions =
        ApiOptions(
            applicationName = props.stringNotEmpty("service.name"),
            serverPort = Port(props.intRequired("server.port")),
            corsPolicy = CorsConfig.from(props).asPolicy(),
            logHttpBody = props.booleanRequired("log.http.body"),
            hotReloadTemplates = props.boolean("dashboard.renderer.hotreload") ?: false,
            updatesPollRate = (props.long("dashboard.client.pollRateSeconds") ?: 5).seconds,
            clientSecretToken =
                ClientSecretToken(props.stringNotEmpty("dashboard.client.secretToken")))
  }
}

@JvmInline
value class Port(val value: Int) {
  init {
    require(value in 0..65535) { "Invalid port" }
  }
}

/** Web browsers must specify this token in the url to authorize access to the dashboard. */
@JvmInline value class ClientSecretToken(val value: String)
