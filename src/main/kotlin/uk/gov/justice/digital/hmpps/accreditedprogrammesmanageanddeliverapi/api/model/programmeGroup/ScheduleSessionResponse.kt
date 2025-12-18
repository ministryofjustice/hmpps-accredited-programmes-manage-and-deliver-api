package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ScheduleSessionResponse(
  @get:JsonProperty("message", required = true)
  @Schema(description = "Success message indicating the session was scheduled")
  val message: String,
)
