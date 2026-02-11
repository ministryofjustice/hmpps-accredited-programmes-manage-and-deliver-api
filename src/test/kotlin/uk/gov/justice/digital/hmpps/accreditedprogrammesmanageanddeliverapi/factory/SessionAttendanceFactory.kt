package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendance
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee

class SessionAttendanceFactory {
  private var attendees: List<SessionAttendee> = listOf(SessionAttendeeFactory().produce())

  fun withAttendees(attendees: List<SessionAttendee>) = apply { this.attendees = attendees }

  fun produce() = SessionAttendance(
    attendees = this.attendees,
  )
}
