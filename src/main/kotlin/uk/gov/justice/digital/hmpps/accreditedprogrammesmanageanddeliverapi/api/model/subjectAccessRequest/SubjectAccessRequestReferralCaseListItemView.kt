package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.time.LocalDate

data class SubjectAccessRequestReferralCaseListItemView(
  val crn: String,
  val hasLdc: Boolean,
  val personName: String,
  val sentenceEndDate: LocalDate?,
  var status: String,
)

fun ReferralCaseListItemViewEntity.toApi() = SubjectAccessRequestReferralCaseListItemView(
  crn = crn,
  hasLdc = hasLdc,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  status = status,
)
