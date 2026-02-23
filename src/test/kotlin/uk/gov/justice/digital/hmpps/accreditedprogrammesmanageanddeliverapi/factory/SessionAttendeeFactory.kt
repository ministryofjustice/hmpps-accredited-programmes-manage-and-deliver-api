package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceCode.ATTC
import java.util.UUID
import kotlin.String

class SessionAttendeeFactory {
  private var referralId: UUID = UUID.randomUUID()
  private var outcomeCode: SessionAttendanceCode = ATTC
  private var sessionNotes: String? = null

  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withOutcomeCode(outcomeCode: SessionAttendanceCode) = apply { this.outcomeCode = outcomeCode }
  fun withSessionNotes(sessionNotes: String?) = apply { this.sessionNotes = sessionNotes }

  fun produce() = SessionAttendee(
    referralId = this.referralId,
    outcomeCode = this.outcomeCode,
    sessionNotes = this.sessionNotes,
  )
}
