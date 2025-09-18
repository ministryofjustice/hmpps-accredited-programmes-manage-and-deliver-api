package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

class ReferralLdcHistoryFactory(
  referral: ReferralEntity? = null,
) {
  private var id: UUID? = null
  private var hasLdc: Boolean = false
  private var createdBy: String? = null
  private var createdAt: LocalDateTime? = null
  private var referral: ReferralEntity = referral ?: ReferralEntityFactory().produce()

  fun withHasLdc(hasLdc: Boolean) = apply { this.hasLdc = hasLdc }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }

  fun produce(): ReferralLdcHistoryEntity = ReferralLdcHistoryEntity(
    id = this.id,
    hasLdc = this.hasLdc,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    referral = this.referral,
  )

  fun produceSet(): MutableSet<ReferralLdcHistoryEntity> = mutableSetOf(produce())
}
