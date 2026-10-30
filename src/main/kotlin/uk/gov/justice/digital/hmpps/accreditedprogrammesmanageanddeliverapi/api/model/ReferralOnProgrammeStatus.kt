package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import java.util.UUID

data class ReferralOnProgrammeStatus(
  @field:Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("referralId", required = true)
  val referralId: UUID,

  @field:Schema(
    example = "X933590",
    required = true,
    description = "The crn associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @Schema(
    example = "ORA community order",
    required = false,
    description = "The type of sentence.",
  )
  @get:JsonProperty("sentenceType", required = false)
  val sentenceType: ReferralEntitySourcedFrom? = null,

  @Schema(example = "2500828798", description = "Event ID of the referral", required = true)
  @get:JsonProperty("eventId", required = true)
  val eventId: String?,

  @field:Schema(description = "The event number from the source system, starts at 1.")
  @get:JsonProperty("eventNumber", required = true)
  val eventNumber: Int?,

  @field:Schema(
    example = "Awaiting assessment",
    required = true,
    description = "The display name of the Referral's current Status",
  )
  @get:JsonProperty("currentStatus", required = true)
  val currentStatus: String,

  @field:Schema(
    example = "true",
    required = true,
    description = "Indicates if the referral is currently on a programme",
  )
  @get:JsonProperty("isOnProgramme", required = true)
  val isOnProgramme: Boolean,
)
