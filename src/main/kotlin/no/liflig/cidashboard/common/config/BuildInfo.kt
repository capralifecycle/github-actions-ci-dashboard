@file:UseSerializers(InstantSerializer::class)

package no.liflig.cidashboard.common.config

import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Properties
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import no.liflig.cidashboard.common.serialization.InstantSerializer
import no.liflig.properties.intRequired
import no.liflig.properties.stringNotNull

@Serializable
data class BuildInfo(
    /** During local development, this will be null. */
    val timestamp: Instant?,
    /** Git commit sha. */
    val commit: String,
    /** Git branch. */
    val branch: String,
    /** CI build number. */
    val number: Int,
) {
  companion object {
    /** Create [BuildInfo] based on keys in `application.properties`. */
    fun from(properties: Properties) =
        BuildInfo(
            timestamp =
                try {
                  Instant.parse(properties.stringNotNull("build.timestamp"))
                } catch (_: DateTimeParseException) {
                  Instant.ofEpochMilli(
                      0L,
                  )
                },
            commit = properties.stringNotNull("build.commit"),
            branch = properties.stringNotNull("build.branch"),
            number =
                try {
                  properties.intRequired("build.number")
                } catch (_: IllegalArgumentException) {
                  0
                },
        )
  }
}
