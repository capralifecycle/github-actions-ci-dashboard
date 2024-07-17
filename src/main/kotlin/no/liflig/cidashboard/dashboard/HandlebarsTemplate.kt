package no.liflig.cidashboard.dashboard

import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import mu.KotlinLogging
import mu.withLoggingContext
import no.liflig.cidashboard.dashboard.MissingHelper.ERROR_PREFIX
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.TemplateRenderer

object Renderer {
  const val TEMPLATE_DIR = "handlebars-htmx-templates"

  val hotReloading: TemplateRenderer by lazy {
    // This will crash if constructed in production, so it must be lazy.
    HandlebarsTemplates().HotReload("src/main/resources/$TEMPLATE_DIR")
  }

  val classpath: TemplateRenderer by lazy {
    HandlebarsTemplates {
          it.with(ConcurrentMapTemplateCache() /*Cache template compilations*/)
              .prettyPrint(true /*Trim template whitespace*/)
              .registerHelperMissing(MissingHelper)
        }
        .CachingClasspath(TEMPLATE_DIR)
  }
}

/** Logs errors and inserts a [ERROR_PREFIX] and template when a block/variable is missing. */
object MissingHelper : Helper<Any> {
  private val log = KotlinLogging.logger {}

  /** If you have bugs, this will show up in your HTML. */
  const val ERROR_PREFIX = "ERROR: NOT FOUND"

  override fun apply(context: Any?, options: Options?): Any {
    withLoggingContext(
        "handlebars.context.class" to context?.javaClass?.name,
        /* Works for any com.github.jknack.handlebars.internal.BaseTemplate: */
        "handlebars.location" to options?.fn?.toString(),
        "handlebars.tagType" to options?.tagType?.name) {
          log.error {
            "Missing helper ${options?.helperName} in ${options?.fn?.text()} at ${options?.fn?.filename()}"
          }
        }

    return "$ERROR_PREFIX ${options?.fn?.text()}"
  }
}
