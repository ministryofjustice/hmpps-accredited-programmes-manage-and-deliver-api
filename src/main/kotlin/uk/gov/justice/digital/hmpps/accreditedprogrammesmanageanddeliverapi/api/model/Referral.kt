package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

data class Referral(
  @Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @Schema(
    example = "John Doe",
    required = true,
    description = "The name of the person associated with this referral.",
  )
  @get:JsonProperty("personName", required = true)
  val personName: String,

  @Schema(
    example = "X12345",
    required = true,
    description = "The CRN identifier of the person associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @Schema(
    example = "2025-07-09T10:15:30",
    required = true,
    description = "The date and time that this referral was created.",
  )
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDateTime,

  @Schema(
    example = "Created",
    required = true,
    description = "The current referral status.",
  )
  @get:JsonProperty("status", required = true)
  val status: String,

  @Schema(
    example = "Cohort",
    required = true,
    description = "The current cohort of a referral",
  )
  @get:JsonProperty("cohort", required = true)
  var cohort: OffenceCohort,
)

fun ReferralEntity.toApi() = Referral(
  id = id!!,
  personName = personName,
  crn = crn,
  createdAt = createdAt,
  status = statusHistories.maxByOrNull { it.createdAt }?.referralStatusDescription?.description ?: "Unknown",
  cohort = cohort,
)
