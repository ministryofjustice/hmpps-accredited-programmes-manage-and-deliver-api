package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewEntity
import java.time.LocalDate
import java.time.Period

data class GroupWaitlistItem(
  val crn: String,
  val personName: String,
  val sentenceEndDate: LocalDate,
  val cohort: OffenceCohort,
  val hasLdc: Boolean,
  val age: Int,
  val sex: String,
  val pdu: String,
  val reportingTeam: String,
  val status: String,
)

fun GroupWaitlistItemViewEntity.toApi() = GroupWaitlistItem(
  crn = crn,
  personName = personName,
  sentenceEndDate = sentenceEndDate,
  cohort = OffenceCohort.fromDisplayName(cohort),
  hasLdc = hasLdc,
  age = Period.between(dateOfBirth, LocalDate.now()).years,
  sex = sex,
  pdu = pduName,
  reportingTeam = reportingTeam,
  status = status,
)
