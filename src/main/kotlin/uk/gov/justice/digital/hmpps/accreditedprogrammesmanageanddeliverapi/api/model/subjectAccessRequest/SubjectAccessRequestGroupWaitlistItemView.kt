package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.time.LocalDate
import java.util.UUID

data class SubjectAccessRequestGroupWaitlistItemView(
  val crn: String,
  val dateOfBirth: LocalDate?,
  val hasLdc: Boolean,
  val personName: String,
  val referralId: UUID,
  val sentenceEndDate: LocalDate?,
  val sex: String?,
  val status: String,
)

fun GroupWaitlistItemViewEntity.toApi() = SubjectAccessRequestGroupWaitlistItemView(
  crn = crn,
  dateOfBirth = dateOfBirth,
  hasLdc = hasLdc,
  personName = personName,
  referralId = referralId,
  sentenceEndDate = sentenceEndDate,
  sex = sex,
  status = status,
)
