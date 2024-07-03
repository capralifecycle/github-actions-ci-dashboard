package no.liflig.cidashboard.examplefeature.api

import no.liflig.cidashboard.examplefeature.MyService
import no.liflig.cidashboard.examplefeature.api.routes.GetExampleRoute

class ExampleApi(myService: MyService) {
  val routes =
      listOf(
          GetExampleRoute(myService).route,
      )

  companion object {
    const val PATH = "/example"
  }
}
