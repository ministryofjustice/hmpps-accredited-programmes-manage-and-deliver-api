package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.util.UUID

@Repository
interface SessionAttendanceRepository : JpaRepository<SessionAttendanceEntity, UUID> {

  // TODO might just have to find one here
  fun findBySessionIdAndGroupMembershipReferralId(
    sessionId: UUID,
    referralId: UUID,
  ): MutableList<SessionAttendanceEntity>
}
