package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionNotesHistoryEntity
import java.time.LocalDateTime
import java.util.UUID

class SessionNotesHistoryEntityFactory(
  attendance: SessionAttendanceEntity? = null,
) {
  private var id: UUID? = null
  private var attendance: SessionAttendanceEntity = attendance ?: SessionAttendanceEntityFactory().produce()
  private var notes: String? = "Session notes"
  private var createdBy: String = "UNKNOWN_USER"
  private var createdAt: LocalDateTime = LocalDateTime.now()
  private var createdByFullName: String? = "Unknown User"

  fun withId(id: UUID?) = apply { this.id = id }
  fun withAttendance(attendance: SessionAttendanceEntity) = apply { this.attendance = attendance }
  fun withNotes(notes: String?) = apply { this.notes = notes }
  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }
  fun withCreatedByFullName(createdByFullName: String?) = apply { this.createdByFullName = createdByFullName }

  fun produce() = SessionNotesHistoryEntity(
    id = this.id,
    attendance = this.attendance,
    notes = this.notes,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    createdByFullName = this.createdByFullName,
  )
}
