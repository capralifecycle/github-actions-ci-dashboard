package no.liflig.baseline.common.http4k

import no.liflig.baseline.common.config.Config
import no.liflig.http4k.setup.filters.http4kOpenTelemetryFilter
import org.http4k.client.JavaHttpClient
import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.filter.ClientFilters

/** Creates an HTTP client with default setup already applied, like tracing and headers. */
fun createDefaultClient(config: Config): HttpHandler =
    ClientFilters.http4kOpenTelemetryFilter()
        .then(ClientFilters.userAgent(config))
        .then(JavaHttpClient())

fun ClientFilters.userAgent(config: Config): Filter = Filter { next: HttpHandler ->
  { req: Request ->
    next(
        req.header("User-Agent", "${config.apiOptions.applicationName}/${config.buildInfo.commit}"))
  }
}
