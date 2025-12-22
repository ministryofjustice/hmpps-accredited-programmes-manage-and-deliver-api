package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class BankHolidaysResponse(
  @JsonProperty("england-and-wales")
  val englandAndWales: Division,

  @JsonProperty("scotland")
  val scotland: Division,

  @JsonProperty("northern-ireland")
  val northernIreland: Division,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Division(
  val division: String,
  val events: List<Event>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Event(
  val title: String,
  val date: String,
  val notes: String,
  val bunting: Boolean,
)
