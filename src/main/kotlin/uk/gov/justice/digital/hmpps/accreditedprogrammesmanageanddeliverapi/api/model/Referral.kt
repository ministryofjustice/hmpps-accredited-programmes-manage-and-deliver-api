package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

data class Referral(
  @get:Schema(
    example = "c98151f4-4081-4c65-9f98-54e63a328c8d",
    required = true,
    description = "The unique id of this referral.",
  )
  @get:JsonProperty("id", required = true)
  val id: UUID,

  @get:Schema(
    example = "John Doe",
    required = true,
    description = "The name of the person associated with this referral.",
  )
  @get:JsonProperty("personName", required = true)
  val personName: String,

  @get:Schema(
    example = "X12345",
    required = true,
    description = "The CRN identifier of the person associated with this referral.",
  )
  @get:JsonProperty("crn", required = true)
  val crn: String,

  @get:Schema(
    example = "2025-07-09T10:15:30",
    required = true,
    description = "The date and time that this referral was created.",
  )
  @get:JsonProperty("createdAt", required = true)
  val createdAt: LocalDateTime,

  @get:Schema(
    example = "Created",
    required = true,
    description = "The current referral status.",
  )
  @get:JsonProperty("status", required = true)
  val status: String,

  @get:Schema(
    example = "Cohort",
    required = true,
    description = "The current cohort of a referral",
  )
  @get:JsonProperty("cohort", required = true)
  var cohort: OffenceCohort,

  @get:Schema(description = "The boolean value of whether the group member has Limited Access Offender (LAO) status")
  var isLimitedAccessOffender: Boolean? = false,

  @get:Schema(description = "The boolean value of whether the group member details are excluded from viewing by the logged-in username")
  var isExcluded: Boolean? = false,
)

fun ReferralEntity.toApi(isLimitedAccessOffender: Boolean? = false, isExcluded: Boolean? = false) = Referral(
  id = id!!,
  personName = personName,
  crn = crn,
  createdAt = createdAt,
  status = statusHistories.maxByOrNull { it.createdAt }?.referralStatusDescription?.description ?: "Unknown",
  cohort = referralCohortHistories.maxByOrNull { it.createdAt }?.cohort ?: OffenceCohort.GENERAL_OFFENCE,
  isLimitedAccessOffender = isLimitedAccessOffender,
  isExcluded = isExcluded,
)
