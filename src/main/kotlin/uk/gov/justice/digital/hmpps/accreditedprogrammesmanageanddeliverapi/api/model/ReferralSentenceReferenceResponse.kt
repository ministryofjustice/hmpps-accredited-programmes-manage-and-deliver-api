package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response returned after updating a referral sentence reference")
data class ReferralSentenceReferenceResponse(
  @Schema(
    example = "Referral with ID: d3f55f38-7c7b-4b6e-9aa1-e7d7f9e3e785 now has the sourceFrom: REQUIREMENT and eventId: 2500828798.",
    required = true,
    description = "The text to show to the user, confirming the referral sentence reference update has taken place",
  )
  @get:JsonProperty("message", required = true)
  val message: String,
)
