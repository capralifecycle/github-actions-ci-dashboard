@file:UseSerializers(RegexSerializer::class)

package no.liflig.cidashboard

import java.time.ZoneId
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import no.liflig.cidashboard.persistence.Persisted
import org.apache.commons.lang3.LocaleUtils
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.Query

@Serializable
@Persisted
data class DashboardConfig(
    val id: DashboardConfigId,
    val orgMatchers: List<OrganizationMatcher> = allMatchers(),
    val locale: String = LocaleUtils.toLocale("en_US").toString(),
    val timezone: String = ZoneId.of("Europe/Oslo").id,
) {
  constructor(id: String) : this(DashboardConfigId(id))
  constructor(
      id: String,
      orgMatchers: List<OrganizationMatcher>
  ) : this(DashboardConfigId(id), orgMatchers)

  companion object {

    val bodyLensOfList = Body.auto<List<DashboardConfig>>().toLens()

    private val json = Json {
      ignoreUnknownKeys = false
      encodeDefaults = true
      isLenient = true
    }

    fun fromJson(jsonString: String): DashboardConfig =
        json.decodeFromString(serializer(), jsonString)

    fun allMatchers() =
        listOf(
            OrganizationMatcher(
                Regex(".*"),
                listOf(RepositoryMatcher(Regex(".*"), listOf(BranchMatcher(Regex(".*")))))))
  }

  fun toJson(): String = json.encodeToString(serializer(), this)
}

@Serializable
@Persisted
@JvmInline
value class DashboardConfigId(val value: String) {
  companion object {
    val queryLens =
        Query.map(nextIn = { DashboardConfigId(it) }, nextOut = { it.value })
            .required("dashboardId", "Id of config dashboard")
  }
}

interface Matcher {
  val matcher: Regex
}

@Serializable
@Persisted
data class OrganizationMatcher(
    override val matcher: Regex,
    val repoMatchers: List<RepositoryMatcher>
) : Matcher

@Serializable
@Persisted
data class RepositoryMatcher(override val matcher: Regex, val branchMatchers: List<BranchMatcher>) :
    Matcher

@Serializable @Persisted data class BranchMatcher(override val matcher: Regex) : Matcher

object RegexSerializer : KSerializer<Regex> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("Regex", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Regex) {
    encoder.encodeString(value.pattern)
  }

  override fun deserialize(decoder: Decoder): Regex {
    val pattern = decoder.decodeString()
    return Regex(pattern)
  }
}
