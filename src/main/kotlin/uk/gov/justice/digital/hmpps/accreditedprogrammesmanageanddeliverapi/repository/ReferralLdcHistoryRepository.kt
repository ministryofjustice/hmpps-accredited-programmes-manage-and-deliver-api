package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import java.util.UUID

@Repository
interface ReferralLdcHistoryRepository : JpaRepository<ReferralLdcHistoryEntity, UUID> {
  fun findTopByReferralIdOrderByCreatedAtDesc(referralId: UUID): ReferralLdcHistoryEntity?
}
