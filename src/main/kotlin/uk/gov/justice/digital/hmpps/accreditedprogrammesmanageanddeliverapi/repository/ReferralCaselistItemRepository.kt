package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaselistItemEntity
import java.util.UUID

interface ReferralCaselistItemRepository : JpaRepository<ReferralCaselistItemEntity, UUID>
