package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.create

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CreateOrUpdateReferralMotivationBackgroundAndNonAssociations(
  @Schema(
    example = "true",
    required = true,
    description = "Boolean value indicating whether the referral maintains innocence.",
  )
  @get:JsonProperty("maintainsInnocence", required = true)
  val maintainsInnocence: Boolean,

  @Schema(
    example = "Motivated to change and improve life circumstances.",
    required = true,
    description = "Information on the motivation to participate in an accredited programme.",
  )
  @get:JsonProperty("motivations", required = true)
  val motivations: String,

  @Schema(
    example = "Other information relevant to the referral.",
    required = true,
    description = "Any other relevant information that should be considered.",
  )
  @get:JsonProperty("otherConsiderations", required = true)
  val otherConsiderations: String,

  @Schema(
    example = "Should not be in a group with a person who has a history of reoffending on a previous accredited programme.",
    required = true,
    description = "Information on any non-associations relevant to the referral.",
  )
  @get:JsonProperty("nonAssociations", required = true)
  val nonAssociations: String,
)
