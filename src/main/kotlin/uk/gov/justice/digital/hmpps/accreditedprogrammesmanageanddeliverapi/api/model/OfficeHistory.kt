package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.OfficeHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

data class OfficeHistory(
  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id the referral linked to the office",
  )
  @get:JsonProperty("referral_id", required = true)
  val referral_id: UUID,

  @Schema(
    example = "Birmingham",
    required = true,
    description = "The name of the office.",
  )
  @get:JsonProperty("officeName", required = true)
  val officeName: String,

  @Schema(
    example = "2025-07-09T10:15:30",
    required = true,
    description = "The date and time that this office referral_id entry was created.",
  )
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDateTime,

  @Schema(
    example = "Dave Davis",
    required = true,
    description = "The name of the person who created this office history entry.",
  )
  @get:JsonProperty("createdByUser", required = true)
  val createdByUser: String,

  @Schema(
    example = "2025-07-09T10:15:30",
    description = "The date and time that this referral to office relationship was deleted.",
  )
  @get:JsonProperty("deletedAt", required = true)
  val deletedAt: LocalDateTime? = null,
)

fun OfficeHistoryEntity.toApi() = OfficeHistory(
  referral_id = referral_id,
  officeName = officeName,
  createdAt = createdAt,
  createdByUser = createdByUser,
  deletedAt = deletedAt
)
