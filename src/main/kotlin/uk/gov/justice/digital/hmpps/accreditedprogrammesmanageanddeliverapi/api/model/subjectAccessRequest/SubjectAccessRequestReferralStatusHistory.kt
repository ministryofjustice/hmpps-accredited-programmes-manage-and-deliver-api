package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.time.LocalDateTime

data class SubjectAccessRequestReferralStatusHistory(
  val additionalDetails: String?,
  val createdBy: String,
  val startDate: LocalDateTime?,
  val referralStatusDescription: SubjectAccessRequestReferralStatusDescription,
)

fun ReferralStatusHistoryEntity.toApi() = SubjectAccessRequestReferralStatusHistory(
  additionalDetails = additionalDetails,
  createdBy = createdBy,
  startDate = startDate,
  referralStatusDescription = referralStatusDescription.toApi(),
)
