package no.liflig.cidashboard.dashboard

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Helper
import com.github.jknack.handlebars.Options
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import com.github.jknack.handlebars.cache.NullTemplateCache
import com.github.jknack.handlebars.helper.StringHelpers
import java.time.Instant
import kotlin.math.abs
import kotlin.math.min
import kotlin.time.Duration
import no.liflig.cidashboard.dashboard.MissingHelper.ERROR_PREFIX
import no.liflig.cidashboard.persistence.CiStatus
import no.liflig.logging.getLogger
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.TemplateRenderer

object Renderer {
  const val TEMPLATE_DIR = "handlebars-htmx-templates"

  private val handlebarsConfig: (Handlebars) -> Handlebars = { handlebars ->
    handlebars
        .prettyPrint(true /*Trim template whitespace*/)
        .registerHelperMissing(MissingHelper)
        .registerHelpers(CustomHelpers)
        .registerHelpers(StringHelpers::class.java)
  }

  val hotReloading: TemplateRenderer by lazy {
    // This will crash if constructed in production because of baseTemplateDir, so it must be lazy.
    HandlebarsTemplates { handlebarsConfig(it).with(NullTemplateCache.INSTANCE) }
        .HotReload("src/main/resources/$TEMPLATE_DIR")
  }

  val classpath: TemplateRenderer by lazy {
    HandlebarsTemplates {
          handlebarsConfig(it).with(ConcurrentMapTemplateCache() /*Cache template compilations*/)
        }
        .CachingClasspath(TEMPLATE_DIR)
  }
}

/** Logs errors and inserts a [ERROR_PREFIX] and template when a block/variable is missing. */
object MissingHelper : Helper<Any> {
  private val log = getLogger()

  /** If you have bugs, this will show up in your HTML. */
  const val ERROR_PREFIX = "ERROR: NOT FOUND"

  override fun apply(context: Any?, options: Options?): Any {
    log.error {
      field("handlebars.context.class", context?.javaClass?.name)
      /* Works for any com.github.jknack.handlebars.internal.BaseTemplate: */
      field("handlebars.location", options?.fn?.toString())
      field("handlebars.tagType", options?.tagType?.name)
      "Missing helper ${options?.helperName} in ${options?.fn?.text()} at ${options?.fn?.filename()}"
    }

    return "$ERROR_PREFIX ${options?.fn?.text()}"
  }
}

object CustomHelpers {
  @Suppress("unused")
  @JvmName("progressPercentage")
  fun progressPercentage(maxTime: Duration?, startTime: Instant, now: Instant): String {
    if (maxTime == null) {
      return "0"
    }
    val elapsedTimeInMillis = abs(now.toEpochMilli() - startTime.toEpochMilli())
    val maxTimeInMillis = maxTime.inWholeMilliseconds
    val percent: Int = min((elapsedTimeInMillis * 100f / maxTimeInMillis).toInt(), 100)
    return percent.toString()
  }

  @Suppress("unused")
  fun isBuilding(status: CiStatus.PipelineStatus): Boolean {
    return when (status) {
      CiStatus.PipelineStatus.QUEUED,
      CiStatus.PipelineStatus.IN_PROGRESS -> true
      else -> false
    }
  }
}
