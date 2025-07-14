package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AvailabilityEntity
import java.util.UUID

@Repository
interface AvailabilityRepository : JpaRepository<AvailabilityEntity, UUID> {

  fun findByReferralId(referralId: UUID): AvailabilityEntity?
}
