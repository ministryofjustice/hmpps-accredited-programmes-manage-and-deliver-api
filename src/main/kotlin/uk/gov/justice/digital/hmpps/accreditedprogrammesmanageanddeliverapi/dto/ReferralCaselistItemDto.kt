package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.dto

import java.time.LocalDateTime

data class ReferralCaselistItemDto(
  val referral: ReferralDto,
  val lastUpdatedAt: LocalDateTime,
  val crn: String,
  val personName: String,
  val probationOffice: String,
  val sentenceEndDate: String? = null,
  val postSentenceSupervisionEndDate: String,
  val cohort: String,
  val referralStatus: String,
)

fun ReferralCaselistItemDto.toDto() = ReferralCaselistItemDto(
  referral = referral,
  lastUpdatedAt = lastUpdatedAt,
  crn = crn,
  personName = personName,
  probationOffice = probationOffice,
  sentenceEndDate = sentenceEndDate,
  postSentenceSupervisionEndDate = postSentenceSupervisionEndDate,
  cohort = cohort,
  referralStatus = referralStatus,
)
