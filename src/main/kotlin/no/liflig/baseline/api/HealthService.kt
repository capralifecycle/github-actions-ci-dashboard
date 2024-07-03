@file:UseSerializers(InstantSerializer::class)

package no.liflig.baseline.api

import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.baseline.common.config.BuildInfo
import no.liflig.baseline.common.serialization.InstantSerializer
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization.auto

private val jsonLens = Body.auto<HealthStatus>().toLens()

class HealthService(
    private val applicationName: String,
    private val buildInfo: BuildInfo,
) {
  private val runningSince: Instant = getRunningSince()

  fun endpoint(): HttpHandler = { Response(Status.OK).with(jsonLens of healthStatus()) }

  private fun healthStatus() =
      HealthStatus(
          name = applicationName,
          timestamp = Instant.now(),
          runningSince = runningSince,
          build = buildInfo,
      )

  private fun getRunningSince(): Instant {
    val uptimeInMillis = ManagementFactory.getRuntimeMXBean().uptime
    return Instant.now().minus(uptimeInMillis, ChronoUnit.MILLIS)
  }
}

@Serializable
data class HealthStatus(
    val name: String,
    val timestamp: Instant,
    val runningSince: Instant,
    val build: BuildInfo,
)
