package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class AllocateToGroupRequest(
  @Schema(
    example = "Alex has been added to the group after a conversation with John Doe",
    required = true,
    description = "Arbitrary text that will be added to the Status History of the Referral",
  )
  @get:JsonProperty("additionalDetails", required = true)
  val additionalDetails: String,
)
