package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class RemoveFromGroupResponse(
  @Schema(
    example = "Alex River was removed from this group. Their referral status is now Awaiting allocation.",
    required = true,
    description = "The text to show to the user, confirming the removal has taken place",
  )
  @get:JsonProperty("message", required = true)
  val message: String,
)
