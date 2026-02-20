package no.liflig.cidashboard.admin.gui

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import com.github.jknack.handlebars.cache.NullTemplateCache
import com.github.jknack.handlebars.helper.StringHelpers
import no.liflig.cidashboard.dashboard.CustomHelpers
import no.liflig.cidashboard.dashboard.MissingHelper
import no.liflig.cidashboard.dashboard.Renderer
import org.http4k.template.HandlebarsTemplates
import org.http4k.template.TemplateRenderer

object AdminRenderer {
  private val handlebarsConfig: (Handlebars) -> Handlebars = { handlebars ->
    handlebars
        .prettyPrint(true)
        .registerHelperMissing(MissingHelper)
        .registerHelpers(CustomHelpers)
        .registerHelpers(StringHelpers::class.java)
  }

  val hotReloading: TemplateRenderer by lazy {
    HandlebarsTemplates { handlebarsConfig(it).with(NullTemplateCache.INSTANCE) }
        .HotReload("src/main/resources/${Renderer.TEMPLATE_DIR}")
  }

  val classpath: TemplateRenderer by lazy {
    HandlebarsTemplates { handlebarsConfig(it).with(ConcurrentMapTemplateCache()) }
        .CachingClasspath(Renderer.TEMPLATE_DIR)
  }
}
