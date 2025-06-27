package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.listener.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SQSMessage(
  @JsonProperty("Message") val message: String,
)
