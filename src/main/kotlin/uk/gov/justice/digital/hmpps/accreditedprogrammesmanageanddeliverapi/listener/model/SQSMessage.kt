package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
  @JsonProperty("Message") val message: String,
)
