package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class EmptyStringToNullDeserializer : JsonDeserializer<String?>() {
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String? {
    val value = p.valueAsString
    return if (value.isNullOrBlank()) null else value
  }
}
