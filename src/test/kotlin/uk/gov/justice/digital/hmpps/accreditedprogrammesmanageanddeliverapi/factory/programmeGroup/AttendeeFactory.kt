package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import java.util.UUID

class AttendeeFactory {
  private var id: UUID? = null
  private var referral: ReferralEntity? = null
  private var session: SessionEntity? = null
  private var sessionAttendances: MutableList<SessionAttendanceEntity> = mutableListOf()

  fun withId(id: UUID?) = apply { this.id = id }
  fun withReferral(referral: ReferralEntity) = apply { this.referral = referral }
  fun withSession(session: SessionEntity) = apply { this.session = session }
  fun withSessionAttendances(sessionAttendances: MutableList<SessionAttendanceEntity>) = apply { this.sessionAttendances = sessionAttendances }

  fun produce() = AttendeeEntity(
    id = this.id,
    referral = referral ?: throw IllegalStateException("Referral must be set"),
    session = session ?: throw IllegalStateException("Session must be set"),
    sessionAttendances = this.sessionAttendances,
  )
}
