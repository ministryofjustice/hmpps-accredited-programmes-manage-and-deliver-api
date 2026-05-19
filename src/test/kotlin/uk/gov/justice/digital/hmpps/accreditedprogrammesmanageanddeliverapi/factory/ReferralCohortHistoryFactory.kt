package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralCohortHistoryFactory(
  private var referral: ReferralEntity? = null,
) {
  private var id: UUID? = null
  private var cohort: OffenceCohort? = null
  private var createdBy: String? = null
  private var createdAt: LocalDateTime? = null

  fun withCohort(cohort: OffenceCohort) = apply { this.cohort = cohort }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }

  fun produce(): ReferralCohortHistoryEntity = ReferralCohortHistoryEntity(
    id = id,
    referral = referral ?: ReferralEntityFactory().produce(),
    cohort = cohort ?: OffenceCohort.GENERAL_OFFENCE,
    createdBy = createdBy ?: "SYSTEM",
    createdAt = createdAt ?: LocalDateTime.now(),
  )

  fun produceSet(): MutableSet<ReferralCohortHistoryEntity> = mutableSetOf(produce())
}
