package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.ReferralEntityFactory
import java.time.LocalDateTime
import java.util.UUID

class ReferralCohortHistoryEntityFactory {
  private var id: UUID? = UUID.randomUUID()
  private var referral: ReferralEntity = ReferralEntityFactory().produce()
  private var cohort: OffenceCohort = OffenceCohort.GENERAL_OFFENCE
  private var createdBy: String = "SYSTEM"
  private var createdAt: LocalDateTime = LocalDateTime.now()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

  fun produce() = ReferralCohortHistoryEntity(
    id = this.id,
    referral = this.referral,
    cohort = this.cohort,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
  )
}
