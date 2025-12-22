package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response returned after updating a referral status")
data class StatusUpdateResult(
  val referralStatusHistory: ReferralStatusHistory,
  @Schema(
    example = "Alex River's referral status is now Recall. They have been removed from group BCCDD1'.",
    required = true,
    description = "The text to show to the user, confirming the status update has taken place",
  )
  @get:JsonProperty("message", required = true)
  val message: String
)