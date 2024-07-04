@file:UseSerializers(InstantSerializer::class)

package no.liflig.cidashboard.health

import java.lang.management.ManagementFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.cidashboard.common.config.BuildInfo
import no.liflig.cidashboard.common.serialization.InstantSerializer
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto

class HealthService(
    private val applicationName: String,
    private val buildInfo: BuildInfo,
) {
  private val runningSince: Instant = getRunningSince()

  fun status() =
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
) {
  companion object {
    val bodyLens = Body.auto<HealthStatus>().toLens()
  }
}
