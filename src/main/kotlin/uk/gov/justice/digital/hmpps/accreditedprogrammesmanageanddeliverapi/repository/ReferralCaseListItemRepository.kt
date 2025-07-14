package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity

interface ReferralCaseListItemRepository : JpaRepository<ReferralCaseListItemViewEntity, String> {
  override fun findAll(
    pageable: Pageable,
  ): Page<ReferralCaseListItemViewEntity>
}
