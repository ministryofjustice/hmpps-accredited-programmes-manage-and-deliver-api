package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.ATTC
import java.util.UUID
import kotlin.String

class SessionAttendeeFactory {
  private var referralId: UUID = UUID.randomUUID()
  private var outcomeCode: SessionAttendanceNDeliusCode = ATTC
  private var sessionNotes: String? = null

  fun withReferralId(referralId: UUID) = apply { this.referralId = referralId }
  fun withOutcomeCode(outcomeCode: SessionAttendanceNDeliusCode) = apply { this.outcomeCode = outcomeCode }
  fun withSessionNotes(sessionNotes: String?) = apply { this.sessionNotes = sessionNotes }

  fun produce() = SessionAttendee(
    referralId = this.referralId,
    outcomeCode = this.outcomeCode,
    sessionNotes = this.sessionNotes,
  )
}
