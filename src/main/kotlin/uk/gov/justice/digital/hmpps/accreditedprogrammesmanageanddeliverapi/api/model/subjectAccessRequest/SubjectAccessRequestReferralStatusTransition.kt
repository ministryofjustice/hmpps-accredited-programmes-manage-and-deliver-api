package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusTransitionEntity

data class SubjectAccessRequestReferralStatusTransition(
  val description: String?,
  val fromStatus: SubjectAccessRequestReferralStatusDescription,
  val toStatus: SubjectAccessRequestReferralStatusDescription,
)

fun ReferralStatusTransitionEntity.toApi() = SubjectAccessRequestReferralStatusTransition(
  description = description,
  fromStatus = fromStatus.toApi(),
  toStatus = toStatus.toApi(),
)
