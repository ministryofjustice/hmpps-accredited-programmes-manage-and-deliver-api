package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusTransitionEntity
import java.util.UUID

interface ReferralStatusTransitionRepository : JpaRepository<ReferralStatusTransitionEntity, UUID> {
  fun findByFromStatusId(fromStatusId: UUID): List<ReferralStatusTransitionEntity>
}
