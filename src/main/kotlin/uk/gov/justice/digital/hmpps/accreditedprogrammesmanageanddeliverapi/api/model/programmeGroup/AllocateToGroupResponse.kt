package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AllocateToGroupResponse(
  @Schema(
    example = "Alex River was added to this group. Their referral status is now Scheduled.",
    required = true,
    description = "The text to show to the user, confirming the allocation has taken place",
  )
  @get:JsonProperty("message", required = true)
  val message: String,
)
