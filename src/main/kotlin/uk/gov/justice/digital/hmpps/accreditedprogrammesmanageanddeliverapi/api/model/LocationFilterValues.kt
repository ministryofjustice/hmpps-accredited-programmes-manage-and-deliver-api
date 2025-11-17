package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class LocationFilterValues(
  @Schema(
    required = true,
    description = "The name of a pdu",
    examples = ["London"],
  )
  @get:JsonProperty("pduName", required = true)
  val pduName: String,
  @Schema(
    required = true,
    description = "List of the reporting teams for a specific pdu",
    examples = ["Team 1", "Team 2"],
  )
  @get:JsonProperty("reportingTeams", required = true)
  val reportingTeams: List<String>,
)
