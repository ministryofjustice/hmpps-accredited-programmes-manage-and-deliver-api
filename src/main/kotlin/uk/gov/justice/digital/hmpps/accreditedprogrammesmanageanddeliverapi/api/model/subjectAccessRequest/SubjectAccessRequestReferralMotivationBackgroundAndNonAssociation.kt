package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import kotlin.String

data class SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation(
  val createdBy: String,
  val lastUpdatedBy: String?,
  val motivation: String?,
  val nonAssociation: String?,
  val otherConsideration: String?,
)

fun ReferralMotivationBackgroundAndNonAssociationsEntity.toApi() = SubjectAccessRequestReferralMotivationBackgroundAndNonAssociation(
  createdBy = createdBy,
  lastUpdatedBy = lastUpdatedBy,
  motivation = motivations,
  nonAssociation = nonAssociations,
  otherConsideration = otherConsiderations,
)
