package no.liflig.cidashboard.common.serialization

import java.math.BigDecimal
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<Instant> {
  private val formatter = DateTimeFormatter.ISO_INSTANT

  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("InstantSerializer", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Instant): Unit =
      encoder.encodeString(formatter.format(value))

  override fun deserialize(decoder: Decoder): Instant =
      formatter.parse(decoder.decodeString(), Instant::from)
}

object BigDecimalSerializer : KSerializer<BigDecimal> {
  override val descriptor: SerialDescriptor =
      PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: BigDecimal) =
      encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): BigDecimal = BigDecimal(decoder.decodeString())
}
