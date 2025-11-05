package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralMotivationBackgroundAndNonAssociationsFactory

fun ReferralMotivationBackgroundAndNonAssociationsFactory.produce(
  id: UUID? = null,
  referral: ReferralEntity = ReferralEntityFactory().produce(),
  maintainsInnocence: Boolean = false,
  motivations: String = randomSentence(),
  nonAssociations: String = randomSentence(),
  otherConsiderations: String = randomSentence(),
  createdBy: String = "APerson",
  createdAt: LocalDateTime = LocalDateTime.now(),
  lastUpdatedAt: LocalDateTime? = null,
  lastUpdatedBy: String? = null,
): ReferralMotivationBackgroundAndNonAssociationsEntity = ReferralMotivationBackgroundAndNonAssociationsEntity(
  id,
  referral = referral,
  maintainsInnocence = maintainsInnocence,
  motivations = motivations,
  nonAssociations = nonAssociations,
  otherConsiderations = otherConsiderations,
  createdBy = createdBy,
  createdAt = createdAt,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedAt = lastUpdatedAt,
)
