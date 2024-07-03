package no.liflig.cidashboard.api

import no.liflig.cidashboard.examplefeature.MyOtherService
import no.liflig.cidashboard.examplefeature.MyService

data class ApiServices(
    val healthService: HealthService,
    val myService: MyService,
    val myOtherService: MyOtherService
)
