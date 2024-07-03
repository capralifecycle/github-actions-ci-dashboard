package no.liflig.cidashboard.examplefeature.api.routes

import com.fasterxml.jackson.annotation.JsonPropertyDescription
import no.liflig.cidashboard.common.http4k.CustomJacksonConfig.auto
import no.liflig.cidashboard.examplefeature.ExampleEntity
import no.liflig.cidashboard.examplefeature.MyService
import no.liflig.cidashboard.examplefeature.api.ExampleApi.Companion.PATH
import org.http4k.contract.RouteMetaDsl
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with

class GetExampleRoute(myService: MyService) {

  private fun meta(): RouteMetaDsl.() -> Unit = {
    summary = "Get all example aggregates"
    operationId = "getExampleAggregates"
    description = "Get a list of all example aggregates"
    tags += Tag("example")

    returning(
        Status.OK,
        ExampleDto.listBodyLens to listOf(ExampleDto.example),
        description = "All examples",
    )
  }

  val route =
      PATH meta
          meta() bindContract
          Method.GET to
          { req ->
            val results = listOf(ExampleEntity()).map { ExampleDto.from(it) }

            Response(Status.OK).with(ExampleDto.listBodyLens of results)
          }
}

data class ExampleDto(
    // This annotation DOES NOT WORK if you use kotlinx.serialization @Serializable on this class.
    @JsonPropertyDescription("The unique id for this entity") val id: String,
) {
  companion object {
    val listBodyLens =
        Body.auto<List<ExampleDto>>()
            .toLens() // by lazy { createBodyLens(ListSerializer(serializer())) }
    // val bodyLens by lazy { createBodyLens(serializer()) }
    // val bodyLens = KotlinxSerialization.autoBody<ExampleDto>().toLens()

    val example = ExampleDto("1")

    fun from(exampleEntity: ExampleEntity) =
        ExampleDto(
            id = exampleEntity.id,
        )
  }
}
