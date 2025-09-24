package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import java.util.UUID

@Repository
interface ReferralStatusHistoryRepository : JpaRepository<ReferralStatusHistoryEntity, UUID> {
  fun findAllByReferralId(referralId: UUID): List<ReferralStatusHistoryEntity>
  fun findFirstByReferralIdOrderByCreatedAtDesc(referralId: UUID): ReferralStatusHistoryEntity?
}
