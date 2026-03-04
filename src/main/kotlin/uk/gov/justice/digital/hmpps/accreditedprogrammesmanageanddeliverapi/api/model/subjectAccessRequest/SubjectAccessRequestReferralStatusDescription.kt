package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity

data class SubjectAccessRequestReferralStatusDescription(
  val description: String,
)

fun ReferralStatusDescriptionEntity.toApi() = SubjectAccessRequestReferralStatusDescription(
  description = description,
)
