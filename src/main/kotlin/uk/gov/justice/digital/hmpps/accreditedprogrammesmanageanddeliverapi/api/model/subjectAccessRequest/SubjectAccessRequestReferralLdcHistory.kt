package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity

data class SubjectAccessRequestReferralLdcHistory(
  val createdBy: String?,
)

fun ReferralLdcHistoryEntity.toApi() = SubjectAccessRequestReferralLdcHistory(
  createdBy = createdBy,
)
