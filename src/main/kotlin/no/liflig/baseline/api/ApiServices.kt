package no.liflig.baseline.api

import no.liflig.baseline.examplefeature.MyOtherService
import no.liflig.baseline.examplefeature.MyService

data class ApiServices(
    val healthService: HealthService,
    val myService: MyService,
    val myOtherService: MyOtherService
)
