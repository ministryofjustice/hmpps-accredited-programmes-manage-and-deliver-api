package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
  @JsonProperty("Type") val type: String,
  @JsonProperty("MessageId") val messageId: UUID,
  @JsonProperty("Token") val token: String?,
  @JsonProperty("TopicArn") val topicArn: String,
  @JsonProperty("SubscribeURL") val subscribeUrl: String?,
  @JsonProperty("Timestamp") val timestamp: ZonedDateTime,
  @JsonProperty("SignatureVersion") val signatureVersion: String?,
  @JsonProperty("Signature") val signature: String?,
  @JsonProperty("SigningCertURL") val signingCertURL: String?,
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
) {
  val eventType: String? @JsonIgnore get() = attributes["eventType"]?.value
}

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {
  constructor(eventType: String) : this(mutableMapOf("eventType" to MessageAttribute("String", eventType)))

  override operator fun get(key: String): MessageAttribute? = attributes[key]
  operator fun set(key: String, value: MessageAttribute) {
    attributes[key] = value
  }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
