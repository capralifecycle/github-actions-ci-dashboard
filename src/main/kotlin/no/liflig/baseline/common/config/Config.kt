package no.liflig.baseline.common.config

import java.util.Properties
import no.liflig.properties.loadProperties

/**
 * Holds configuration of the service.
 *
 * @see [Config.load]
 */
data class Config(
    private val properties: Properties,
    val buildInfo: BuildInfo = BuildInfo.from(properties),
    val apiOptions: ApiOptions = ApiOptions.from(properties),
    val database: DbConfig = DbConfig.from(properties)
) {

  companion object {
    /**
     * Creates a new instance based on `application.properties` and AWS Parameter Store (if
     * available).
     */
    fun load() = Config(loadProperties())
  }
}
