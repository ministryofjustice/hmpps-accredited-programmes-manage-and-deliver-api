package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import kotlin.String

class SessionAttendanceEntityFactory(
  session: SessionEntity? = null,
  groupMembership: ProgrammeGroupMembershipEntity? = null,
  recordedByFacilitator: FacilitatorEntity? = null,
) {
  private var id: UUID = UUID.randomUUID()
  private var session: SessionEntity = session ?: SessionFactory().produce()
  private var groupMembership: ProgrammeGroupMembershipEntity =
    groupMembership ?: ProgrammeGroupMembershipFactory().produce()
  private var attended: Boolean = true
  private var didNotEngage: Boolean = true
  private var legitimateAbsence: Boolean = true
  private var notes: String = "Notes 1"
  private var recordedByFacilitator: FacilitatorEntity = recordedByFacilitator ?: FacilitatorEntityFactory().produce()
  private var recordedAt: LocalDateTime? = LocalDateTime.now(ZoneId.of("UTC"))

  fun withId(id: UUID) = apply { this.id = id }
  fun withSession(session: SessionEntity) = apply { this.session = session }
  fun withGroupMembership(groupMembership: ProgrammeGroupMembershipEntity) = apply { this.groupMembership = groupMembership }

  fun withAttended(attended: Boolean) = apply { this.attended = attended }
  fun withDidNotEngage(didNotEngage: Boolean) = apply { this.didNotEngage = didNotEngage }
  fun withLegitimateAbsence(legitimateAbsence: Boolean) = apply { this.legitimateAbsence = legitimateAbsence }
  fun withNotes(notes: String) = apply { this.notes = notes }
  fun withRecordedByFacilitator(recordedByFacilitator: FacilitatorEntity) = apply { this.recordedByFacilitator = recordedByFacilitator }

  fun withRecordedAt(recordedAt: LocalDateTime) = apply { this.recordedAt = recordedAt }

  fun produce() = SessionAttendanceEntity(
    id = this.id,
    session = this.session,
    groupMembership = this.groupMembership,
    attended = this.attended,
    didNotEngage = this.didNotEngage,
    legitimateAbsence = this.legitimateAbsence,
    notes = this.notes,
    recordedByFacilitator = this.recordedByFacilitator,
    recordedAt = this.recordedAt,
  )
}
