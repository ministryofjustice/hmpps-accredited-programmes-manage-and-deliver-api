package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class SessionAttendanceEntityFactory(
  recordedByFacilitator: FacilitatorEntity? = null,
) {
  private var id: UUID? = null
  private var attendee: AttendeeEntity? = null
  private var legitimateAbsence: Boolean = true
  private var recordedByFacilitator: FacilitatorEntity = recordedByFacilitator ?: FacilitatorEntityFactory().produce()
  private var recordedAt: LocalDateTime? = LocalDateTime.now(ZoneId.of("UTC"))
  private var outcomeType: SessionAttendanceNDeliusOutcomeEntity =
    SessionAttendanceNDeliusOutcomeEntityFactory().produce()
  private var createdBy: String = "UNKNOWN_USER"
  private var createdAt: LocalDateTime = LocalDateTime.now()

  fun withId(id: UUID) = apply { this.id = id }

  fun withLegitimateAbsence(legitimateAbsence: Boolean) = apply { this.legitimateAbsence = legitimateAbsence }
  fun withRecordedByFacilitator(recordedByFacilitator: FacilitatorEntity) = apply { this.recordedByFacilitator = recordedByFacilitator }

  fun withAttendee(attendee: AttendeeEntity) = apply { this.attendee = attendee }
  fun withRecordedAt(recordedAt: LocalDateTime) = apply { this.recordedAt = recordedAt }

  fun withOutcomeType(outcomeType: SessionAttendanceNDeliusOutcomeEntity) = apply { this.outcomeType = outcomeType }

  fun withCreatedBy(createdBy: String) = apply { this.createdBy = createdBy }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

  fun produce() = SessionAttendanceEntity(
    id = this.id,
    attendee = attendee ?: throw IllegalStateException("Attendee must be provided"),
    legitimateAbsence = this.legitimateAbsence,
    recordedByFacilitator = this.recordedByFacilitator,
    recordedAt = this.recordedAt,
    outcomeType = this.outcomeType,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
  )
}
