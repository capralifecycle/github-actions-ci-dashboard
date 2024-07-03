package no.liflig.cidashboard.common.http4k

import org.eclipse.jetty.server.HttpConnectionFactory
import org.http4k.server.ConnectorBuilder
import org.http4k.server.http

/** Avoid leaking Jetty version in http response header "Server". */
fun httpNoServerVersionHeader(port: Int): ConnectorBuilder = { server ->
  http(port)(server).apply {
    connectionFactories.filterIsInstance<HttpConnectionFactory>().forEach {
      it.httpConfiguration.sendServerVersion = false
    }
  }
}
