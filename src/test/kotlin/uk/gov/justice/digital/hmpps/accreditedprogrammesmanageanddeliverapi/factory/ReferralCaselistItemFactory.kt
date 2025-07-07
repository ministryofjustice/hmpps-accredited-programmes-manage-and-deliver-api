package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomUppercaseString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaselistItemEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralCaselistItemFactory

fun ReferralCaselistItemFactory.produce(
  id: UUID = UUID.randomUUID(),
  referral: ReferralEntity = ReferralEntityFactory().produce(),
  lastUpdatedAt: LocalDateTime = LocalDateTime.now(),
  crn: String = randomUppercaseString(6),
  personName: String = randomSentence(wordRange = 1..3),
  probationOffice: String = randomSentence(wordRange = 1..3),
  sentenceEndDate: LocalDateTime = LocalDateTime.now(),
  pssEndDate: LocalDateTime? = null,
  cohort: String = "General Offence",
  referralStatus: String = randomUppercaseString(6),
) = ReferralCaselistItemEntity(
  id,
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
