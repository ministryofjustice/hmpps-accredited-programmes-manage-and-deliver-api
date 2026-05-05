package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import java.time.LocalDateTime

data class SubjectAccessRequestReferralLdcHistory(
  val createdBy: String?,
  val createdAt: LocalDateTime?,
  val hasLdc: Boolean,
)

fun ReferralLdcHistoryEntity.toApi() = SubjectAccessRequestReferralLdcHistory(
  createdBy = createdBy,
  createdAt = createdAt,
  hasLdc = hasLdc,
)
