package test.util

import java.io.File
import no.liflig.logging.getLogger

private val log = getLogger()

/** Use the same postgres image for testing as we use when running app locally. */
fun extractPostgresImageFromDockerCompose(): String {
  var dir = File(System.getProperty("user.dir"))
  var dockerComposeFile: File

  // Traverse up to find docker-compose.yml.
  while (true) {
    dockerComposeFile = dir.resolve("docker-compose.yml")

    if (dockerComposeFile.exists()) break

    check(dir.parentFile != dir) { "Could not find docker-compose.yml" }
    dir = dir.parentFile
  }

  return dockerComposeFile
      .readText()
      .lines()
      .first { "image: postgres" in it }
      .substringAfter("image:")
      .trim()
      .also { log.info { "Using postgres image [$it]" } }
}
