/**
 * This file is a collection of all OpenTelemetry metrics factories, used to create counters and
 * gauges etc.
 *
 * The metrics' units follow UCUM http://unitsofmeasure.org/ucum.html. This means that unit is case
 * sensitive, and can be things like: 1, s, ms, kB, B, mbps, and so on.
 *
 * Units can also contain an annotation, which is a word in brackets: "{events}", "{failures}",
 * "%{ratio}", "s{latency}", and so on. An annotation without a unit is the default unit "1".
 * "{events}" means "unit of 1, with annotation events".
 *
 * Metric names should use lowercase and underscores: my_metric_name.
 *
 * Descriptions can be anything, and explains the metric for a human.
 */
package no.liflig.cidashboard.common.observability

import io.opentelemetry.api.metrics.LongCounter

object AuthMetrics {
  fun logins(): LongCounter =
      OpenTelemetryConfig.meter
          .counterBuilder("logins")
          .setDescription("How many users have authenticated")
          .setUnit("{logins}")
          .build()
}

object RepositoryMetrics {
  // Add your own (updown)counters, gauges, histograms etc here.
}
