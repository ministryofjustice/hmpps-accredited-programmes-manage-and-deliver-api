package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCohortHistoryEntity
import java.util.UUID

@Repository
interface ReferralCohortHistoryRepository : JpaRepository<ReferralCohortHistoryEntity, UUID> {
  fun findTopByReferralIdOrderByCreatedAtDesc(referralId: UUID): ReferralCohortHistoryEntity?
}
