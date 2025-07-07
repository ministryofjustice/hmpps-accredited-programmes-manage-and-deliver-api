package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto

import java.time.LocalDateTime

data class ReferralCaselistItemEntityDto(
  val referral: ReferralEntityDto,
  val lastUpdatedAt: LocalDateTime,
  val crn: String,
  val personName: String,
  val probationOffice: String,
  val sentenceEndDate: String? = null,
  val pssEndDate: String,
  val cohort: String,
  val referralStatus: String,
)

fun ReferralCaselistItemEntityDto.toDto() = ReferralCaselistItemEntityDto(
  referral = referral,
  lastUpdatedAt = lastUpdatedAt,
  crn = crn,
  personName = personName,
  probationOffice = probationOffice,
  sentenceEndDate = sentenceEndDate,
  pssEndDate = pssEndDate,
  cohort = cohort,
  referralStatus = referralStatus,
)
