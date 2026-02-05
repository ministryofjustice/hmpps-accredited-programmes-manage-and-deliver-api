package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class EditSessionDateAndTimeResponse(
  @Schema(
    example = "The date and time and schedule have been updated.",
    required = true,
    description = "The text to show to the user, confirming the update has taken place",
  )
  @get:JsonProperty("message", required = true)
  val message: String,
)
