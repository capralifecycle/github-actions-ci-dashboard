package no.liflig.baseline.examplefeature.api

import no.liflig.baseline.examplefeature.MyService
import no.liflig.baseline.examplefeature.api.routes.GetExampleRoute

class ExampleApi(myService: MyService) {
  val routes =
      listOf(
          GetExampleRoute(myService).route,
      )

  companion object {
    const val PATH = "/example"
  }
}
