package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.ReferralStatusType

@Schema(description = "Details of a referral status change event")
data class ReferralStatusInfo(
  @field:Schema(description = "The new status of the referral", required = true)
  val newStatus: ReferralStatusType,
  @field:Schema(description = "The type of entity from which this status change was sourced", required = true)
  val sourcedFromEntityType: ReferralEntitySourcedFrom,
  @field:Schema(description = "The ID of the entity from which this status change was sourced", required = true)
  val sourcedFromEntityId: Long,
  @field:Schema(
    description = "The event number associated with the requirement/licence condition for this referral",
    required = true,
  )
  val eventNumber: Int,
  @field:Schema(description = "Optional notes associated with the status change")
  val notes: String?,
  @field:Schema(description = "A human-readable description of the status change", required = true)
  val description: String,
  @field:Schema(description = "Username of the person who made the status update", required = true)
  val username: String,
)
