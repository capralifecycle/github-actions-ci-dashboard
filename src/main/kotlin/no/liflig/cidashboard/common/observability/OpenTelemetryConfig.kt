package no.liflig.cidashboard.common.observability

import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import java.util.logging.Level
import java.util.logging.Logger
import mu.KotlinLogging
import org.slf4j.bridge.SLF4JBridgeHandler

/**
 * Call the [OpenTelemetryConfig.configure] method.
 *
 * See
 * [opentelemetry.io/docs/instrumentation/java/manual_instrumentation](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/)
 * for usage of the SDK.
 */
class OpenTelemetryConfig {
  private val log = KotlinLogging.logger {}

  companion object {
    private const val INSTRUMENTATION_NAME =
        "opentelemetry-instrumentation-GitHubActionsCiDashboard"
    private const val INSTRUMENTATION_VERSION = "1.0.0"

    val meter: Meter by lazy {
      GlobalOpenTelemetry.get()
          .meterBuilder(INSTRUMENTATION_NAME)
          .setInstrumentationVersion(INSTRUMENTATION_VERSION)
          .build()
    }

    val tracer: Tracer by lazy {
      GlobalOpenTelemetry.get()
          .tracerBuilder(INSTRUMENTATION_NAME)
          .setInstrumentationVersion(INSTRUMENTATION_VERSION)
          .build()
    }
  }

  fun configure() =
      try {
        log.info { "Configuring OpenTelemetry" }

        // OTel uses JUL at debug level
        Logger.getLogger("io.opentelemetry").level = Level.WARNING
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        // The java agent will configure the GlobalOpenTelemetry, so no opentelemetry-sdk is needed
        // here.

        log.info { "Configuration of OpenTelemetry complete" }
      } catch (e: Throwable) {
        log.error(e) { "Failed to configure OpenTelemetry" }
      }
}

/**
 * Use this when creating a new span with [OpenTelemetryConfig.tracer] to activate it and
 * automatically end the span afterward.
 */
fun <R> SpanBuilder.use(block: Span.() -> R): R {
  val span = this.startSpan()
  try {
    span.makeCurrent().use {
      return span.block()
    }
  } catch (ex: Throwable) {
    span.recordException(ex)
    span.setStatus(StatusCode.ERROR)
    throw ex
  } finally {
    span.end()
  }
}

/* Uncomment if you are using Coroutines.
suspend fun <R> SpanBuilder.useSuspending(block: suspend Span.() -> R): R {
  val span = this.startSpan()
  try {
    return withContext(span.asContextElement()) { span.block() }
  } catch (ex: Throwable) {
    span.recordException(ex)
    span.setStatus(StatusCode.ERROR)
    throw ex
  } finally {
    span.end()
  }
}*/
