package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
  @JsonProperty("MessageId") val messageId: UUID,
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
) {
  val eventType: String? @JsonIgnore get() = attributes["eventType"]?.value
}

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {
  constructor(eventType: String, detailUrl: String) : this(
    mutableMapOf(
      "eventType" to MessageAttribute(
        "String",
        eventType,
      ),
    ),
  )

  override operator fun get(key: String): MessageAttribute? = attributes[key]
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
