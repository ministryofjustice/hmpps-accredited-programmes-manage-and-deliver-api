package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import java.time.LocalDate
import java.util.UUID
import kotlin.String

class SessionAttendeeFactory {
  private var attendeeId: UUID = UUID.randomUUID()
  private var name: String = "John Smith"
  private var attended: Boolean = true
  private var recordedAt: LocalDate = LocalDate.now()
  private var recordedByFacilitatorId: UUID = UUID.randomUUID()
  private var sessionNotes: String? = null

  fun withAttendeeId(attendeeId: UUID) = apply { this.attendeeId = attendeeId }
  fun withName(name: String) = apply { this.name = name }
  fun withAttended(attended: Boolean) = apply { this.attended = attended }
  fun withRecordedAt(recordedAt: LocalDate) = apply { this.recordedAt = recordedAt }
  fun withRecordedByFacilitatorId(recordedByFacilitatorId: UUID) = apply { this.recordedByFacilitatorId = recordedByFacilitatorId }
  fun withSessionNotes(sessionNotes: String?) = apply { this.sessionNotes = sessionNotes }

  fun produce() = SessionAttendee(
    attendeeId = this.attendeeId,
    name = this.name,
    attended = this.attended,
    recordedAt = this.recordedAt,
    recordedByFacilitatorId = this.recordedByFacilitatorId,
    sessionNotes = this.sessionNotes,
  )
}
