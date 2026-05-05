package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import java.time.LocalDateTime

data class SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation(
  val createdBy: String,
  val createdAt: LocalDateTime,
  val lastUpdatedBy: String?,
  val maintainsInnocence: Boolean?,
  val motivation: String?,
  val nonAssociation: String?,
  val otherConsideration: String?,
)

fun ReferralMotivationBackgroundAndNonAssociationsEntity.toApi() = SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation(
  createdBy = createdBy,
  createdAt = createdAt,
  lastUpdatedBy = lastUpdatedBy,
  maintainsInnocence = maintainsInnocence,
  motivation = motivations,
  nonAssociation = nonAssociations,
  otherConsideration = otherConsiderations,
)
