package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class CreateReferralStatusHistory(
  @get:JsonProperty("referralStatusDescriptionId", required = true)
  @Schema(
    example = "76b2f8d8-260c-4766-a716-de9325292609",
    description = "The UUID of the relevant Referral Status Description",
    required = true,
  )
  val referralStatusDescriptionId: UUID,

  @get:JsonProperty("additionalDetails", required = true)
  @Schema(
    example = "Updating the status following a one-to-one meeting with Person on Probation",
    description = "A free-text description that allows a user to add context or information to the Status change",
    required = false,
  )
  val additionalDetails: String? = null,
)
