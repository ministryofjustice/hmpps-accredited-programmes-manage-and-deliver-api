package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import java.time.LocalDateTime

data class ReferralCaselistItem(
  val referral: Referral,
  val lastUpdatedAt: LocalDateTime,
  val crn: String,
  val personName: String,
  val probationOffice: String,
  val sentenceEndDate: String? = null,
  val postSentenceSupervisionEndDate: String,
  val cohort: String,
  val referralStatus: String,
)

fun ReferralCaselistItem.toApi() = ReferralCaselistItem(
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
