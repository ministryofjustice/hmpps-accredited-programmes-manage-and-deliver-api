package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class RemoveFromGroupRequest(
  @Schema(
    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    required = true,
    description = "The UUID of the referral status description to transition the referral to after removal from the group",
  )
  @get:JsonProperty("referralStatusDescriptionId", required = true)
  val referralStatusDescriptionId: UUID,
  @Schema(
    example = "Alex has been removed from the group due to conflicting commitments",
    required = true,
    description = "Arbitrary text that will be added to the Status History of the Referral",
  )
  @get:JsonProperty("additionalDetails", required = true)
  val additionalDetails: String,
)
