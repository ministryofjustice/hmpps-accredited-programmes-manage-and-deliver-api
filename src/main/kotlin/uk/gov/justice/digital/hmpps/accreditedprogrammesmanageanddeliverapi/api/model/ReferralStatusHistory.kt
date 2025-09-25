package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDateTime
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

  @Schema(
    example = "I have met with xxx, and the assessment has been completed",
    required = true,
    description = "Notes from the user, or possibly the system, to explain the change in status",
  )
  @get:JsonProperty("additionalDetails", required = false)
  val additionalDetails: String?,

  @Schema(
    example = "John Doe",
    required = true,
    description = "The name of the User who updated the status.  SYSTEM and UNKNOWN_USER are known non-human values.",
  )
  @get:JsonProperty("updatedBy", required = false)
  val updatedBy: String,

  @Schema(
    example = "2025-09-25T06:50:20.149Z",
    required = true,
    description = "The time when the Status was changed",
  )
  @get:JsonProperty("updatedAt", required = false)
  val updatedAt: LocalDateTime,

  @field:Schema(description = "The display colour of the status tag")
  @get:JsonProperty("tagColour", required = true)
  val tagColour: String,
)

fun ReferralStatusHistoryEntity.toApi(): ReferralStatusHistory = ReferralStatusHistory(
  id = id!!,
  referralStatusDescriptionId = referralStatusDescription.id,
  referralStatusDescriptionName = referralStatusDescription.description,
  additionalDetails = additionalDetails,
  updatedBy = createdBy,
  updatedAt = createdAt,
  tagColour = referralStatusDescription.labelColour,
)
