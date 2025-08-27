package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import java.util.UUID

@Repository
interface ReferralCaseListItemRepository :
  JpaRepository<ReferralCaseListItemViewEntity, UUID>,
  JpaSpecificationExecutor<ReferralCaseListItemViewEntity>,
  ReferralCaseListItemRepositoryCustom
