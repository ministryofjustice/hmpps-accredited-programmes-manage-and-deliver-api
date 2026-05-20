package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import java.time.LocalDateTime

data class SubjectAccessRequestReferralCohortHistory(
  val createdBy: String?,
  val createdAt: LocalDateTime?,
  val cohort: String?,
)

fun ReferralCohortHistoryEntity.toApi() = SubjectAccessRequestReferralCohortHistory(
  createdBy = createdBy,
  createdAt = createdAt,
  cohort = cohort.displayName,
)
