package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class ReferralStatusHistory(
  @Schema(
    example = "21da0995-5827-4cc9-bbc9-c7c7f2975163",
    required = true,
    description = "ID of the ReferralStatusHistory entry",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "21da0995-5827-4cc9-bbc9-c7c7f2975163",
    required = true,
    description = "ID of the Referral Status Description",
  )
  @get:JsonProperty("referralStatusDescriptionId", required = true)
  val referralStatusDescriptionId: UUID,

  @Schema(
    example = "Awaiting assessment",
    required = true,
    description = "Human-readable name of the Referral Status, useful for UIs",
  )
  @get:JsonProperty("referralStatusDescriptionName", required = true)
  val referralStatusDescriptionName: String,
)
