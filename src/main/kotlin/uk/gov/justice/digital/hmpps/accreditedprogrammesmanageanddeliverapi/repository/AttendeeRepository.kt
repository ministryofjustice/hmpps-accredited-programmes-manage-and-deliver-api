package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.util.UUID

@Repository
interface AttendeeRepository : JpaRepository<AttendeeEntity, UUID> {
  fun findByReferral(referral: ReferralEntity): List<AttendeeEntity>
  fun findByReferralIdIn(referrals: List<UUID>): List<AttendeeEntity>
  fun findByReferralAndSession(referral: ReferralEntity, session: SessionEntity): AttendeeEntity?
}
