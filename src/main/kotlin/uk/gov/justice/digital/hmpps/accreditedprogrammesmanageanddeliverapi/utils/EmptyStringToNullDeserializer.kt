package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class EmptyStringToNullDeserializer : ValueDeserializer<Any?>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
    val value = p.string
    return if (value.isNullOrBlank()) null else value
  }
}
