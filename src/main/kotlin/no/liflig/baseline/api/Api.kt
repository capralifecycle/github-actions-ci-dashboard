package no.liflig.baseline.api

import com.fasterxml.jackson.databind.JsonNode
import no.liflig.baseline.common.config.ApiOptions
import no.liflig.baseline.common.http4k.CustomJacksonConfig
import no.liflig.baseline.examplefeature.api.ExampleApi
import no.liflig.http4k.setup.LifligBasicApiSetup
import no.liflig.http4k.setup.LifligUserPrincipalLog
import no.liflig.http4k.setup.errorhandling.ContractLensErrorResponseRenderer
import org.http4k.contract.ContractRoutingHttpHandler
import org.http4k.contract.bind
import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.contract.jsonschema.v3.AutoJsonToJsonSchema
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.ApiRenderer
import org.http4k.contract.openapi.cached
import org.http4k.contract.openapi.v3.Api
import org.http4k.contract.openapi.v3.ApiServer
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.BasicAuthSecurity
import org.http4k.contract.ui.swaggerUiLite
import org.http4k.core.Method
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

/**
 * Api setup containing the following :
 * - Contract (openapi-schema) related setup
 * - Any additional filters. e.g. auth filter.
 * - Mapping to [LifligUserPrincipalLog] if needed (in combination with auth filter)
 * - Routes
 *
 * Try to keep the more technical stuff/noise in [ApiServer] to avoid code overload.
 */
class Api(
    private val basicApiSetup: LifligBasicApiSetup,
    private val options: ApiOptions,
    private val services: ApiServices,
) {
  fun create(): RoutingHttpHandler {
    /*
     TODO: Add auth lens and logic for principal log mapping if needed.
       Also requires auth filter that puts auth context in lens.
     fun MyAuthLens.toPrincipalLog(): (Request) -> LifligUserPrincipalLog? = ..
     val myAuthLens: MyAuthLens = RequestContextKey.optional(contexts)
     val principalLog = myAuthLens.toPrincipalLog()
    */
    val (coreFilters, errorResponseRenderer) = basicApiSetup.create(principalLog = { null })

    return coreFilters
        // TODO: Add additional filters not provided by coreFilters here. E.g. authFilter.
        .then(
            routes(
                "/api" / "v1" bind contractApi(errorResponseRenderer),
                "/health" bind Method.GET to services.healthService.endpoint(),
                swaggerUiLite { url = "/api/docs/openapi-schema.json" }))
  }

  private fun contractApi(
      errorResponseRenderer: ContractLensErrorResponseRenderer,
  ): ContractRoutingHttpHandler = contract {
    renderer = openApi3Renderer(errorResponseRenderer)
    descriptionPath = "/docs/openapi-schema.json"
    descriptionSecurity = BasicAuthSecurity("master", options.openApiCredentials)
    routes += ExampleApi(services.myService).routes
  }

  private fun openApi3Renderer(
      errorResponseRenderer: ContractLensErrorResponseRenderer
  ): OpenApi3<JsonNode> {
    val jacksonConfig = CustomJacksonConfig
    return OpenApi3(
        apiInfo =
            ApiInfo(
                title = options.applicationName,
                version = "1",
                description = "A REST API for my service",
            ),
        servers = listOf(ApiServer(Uri.of(options.serverBaseUrl))),
        json = jacksonConfig,
        apiRenderer =
            ApiRenderer.Auto<Api<JsonNode>, JsonNode>(
                    jacksonConfig, schema = AutoJsonToJsonSchema(jacksonConfig))
                .cached(),
        errorResponseRenderer = errorResponseRenderer,
    )
  }
}
