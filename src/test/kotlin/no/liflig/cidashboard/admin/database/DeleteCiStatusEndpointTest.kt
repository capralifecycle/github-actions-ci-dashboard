package no.liflig.cidashboard.admin.database

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.liflig.cidashboard.persistence.CiStatusId
import org.assertj.core.api.Assertions.assertThat
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.lens.LensFailure
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class DeleteCiStatusEndpointTest {

  @Test
  fun `should fail when no token in query`() {
    // Given
    val service: DeleteDatabaseRowsService = mockk()
    val endpoint = DeleteCiStatusEndpoint(service)
    val request = Request(Method.DELETE, "/admin/delete")

    // When
    assertThrows<LensFailure> { endpoint(request) }

    // Then
    verify(inverse = true) { service.deleteAll() }
  }

  @Test
  fun `should succeed when token is in query`() {
    // Given
    val service: DeleteDatabaseRowsService = mockk {
      every { deleteById(CiStatusId(any())) } just Runs
    }
    val endpoint = DeleteCiStatusEndpoint(service)
    val request =
        Request(Method.DELETE, "/admin/delete")
            .query("secret", "do-harm")
            .query("id", "120856855-master")

    // When
    val actualResponse = endpoint(request)

    // Then
    assertThat(actualResponse.status).isEqualTo(Status.OK)
    verify { service.deleteById(CiStatusId("120856855-master")) }
  }
}
