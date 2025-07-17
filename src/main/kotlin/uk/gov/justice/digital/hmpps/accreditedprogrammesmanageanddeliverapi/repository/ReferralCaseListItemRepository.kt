package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.util.UUID

interface ReferralCaseListItemRepository : JpaRepository<ReferralCaseListItemViewEntity, UUID> {
  override fun findAll(
    pageable: Pageable,
  ): Page<ReferralCaseListItemViewEntity>
}
