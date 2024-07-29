package no.liflig.cidashboard.admin.database

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.lens.LensFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteAllDatabaseRowsEndpointTest {

  @Test
  fun `should fail when no token in query`() {
    // Given
    val service: DeleteAllDatabaseRowsService = mockk()
    val endpoint = DeleteAllDatabaseRowsEndpoint(service)
    val request = Request(Method.POST, "/admin/nuke")

    // When
    assertThrows<LensFailure> { endpoint(request) }

    // Then
    verify(inverse = true) { service.deleteAll() }
  }

  @Test
  fun `should succeed when token is in query`() {
    // Given
    val service: DeleteAllDatabaseRowsService = mockk() { every { deleteAll() } just Runs }
    val endpoint = DeleteAllDatabaseRowsEndpoint(service)
    val request = Request(Method.POST, "/admin/nuke").query("secret", "do-harm")

    // When
    val actualResponse = endpoint(request)

    // Then
    assertThat(actualResponse.status).isEqualTo(Status.OK)
    verify { service.deleteAll() }
  }
}
