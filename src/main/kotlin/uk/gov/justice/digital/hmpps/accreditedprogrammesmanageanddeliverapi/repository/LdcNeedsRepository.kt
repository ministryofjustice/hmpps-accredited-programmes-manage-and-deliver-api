package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.LdcNeedsEntity
import java.util.UUID

interface LdcNeedsRepository : JpaRepository<LdcNeedsEntity, UUID> {
  fun findByReferralId(referralId: UUID): LdcNeedsEntity?
}
