package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.randomSentence
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralMotivationBackgroundAndNonAssociationsEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralMotivationBackgroundAndNonAssociationsFactory {
  private var id: UUID? = null
  private var referral: ReferralEntity = ReferralEntityFactory().produce()
  private var maintainsInnocence: Boolean = false
  private var motivations: String = randomSentence()
  private var nonAssociations: String = randomSentence()
  private var otherConsiderations: String = randomSentence()
  private var createdBy: String = "APerson"
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var lastUpdatedAt: LocalDateTime? = null
  private var lastUpdatedBy: String? = null

  fun withId(id: UUID) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withMaintainsInnocence(maintainsInnocence: Boolean) = apply { this.maintainsInnocence = maintainsInnocence }
  fun withMotivations(motivations: String) = apply { this.motivations = motivations }
  fun withNonAssociations(nonAssociations: String) = apply { this.nonAssociations = nonAssociations }
  fun withOtherConsents(otherConsents: String) = apply { this.otherConsiderations = otherConsents }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withLastUpdatedAt(lastUpdatedAt: LocalDateTime) = apply { this.lastUpdatedAt = lastUpdatedAt }
  fun withLastUpdatedBy(lastUpdatedBy: String) = apply { this.lastUpdatedBy = lastUpdatedBy }

  fun produce(): ReferralMotivationBackgroundAndNonAssociationsEntity = ReferralMotivationBackgroundAndNonAssociationsEntity(
    id = id,
    referral = referral,
    maintainsInnocence = maintainsInnocence,
    motivations = motivations,
    nonAssociations = nonAssociations,
    otherConsiderations = otherConsiderations,
    createdBy = createdBy,
    createdAt = createdAt,
    lastUpdatedAt = lastUpdatedAt,
    lastUpdatedBy = lastUpdatedBy,
  )
}
