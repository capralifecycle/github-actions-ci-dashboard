package no.liflig.cidashboard.common.http4k

import java.util.Properties

object Webjars {

  /** E.g. `"2.0.5"`. */
  val htmxVersion = readVersion("htmx.org")
  val idiomorphVersion = readVersion("idiomorph")

  /**
   * A webjar includes a `pom.properties` with `version`, so we can read it out here. The version is
   * needed because the webjar includes it in the path, so a html file must know the version.
   */
  @Throws(IllegalStateException::class)
  fun readVersion(webjarFolder: String): String {
    val webJarProperties = Properties()
    javaClass.classLoader
        .getResourceAsStream("META-INF/maven/org.webjars.npm/$webjarFolder/pom.properties")
        .use { webJarProperties.load(it) }

    val version: String? = webJarProperties.getProperty("version")
    check(version != null) { "No version found for $webjarFolder" }

    return version
  }
}
