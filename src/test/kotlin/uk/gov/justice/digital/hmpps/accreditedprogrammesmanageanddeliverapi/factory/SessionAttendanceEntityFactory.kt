package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceOutcomeType.ATTC
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.ProgrammeGroupMembershipFactory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup.SessionFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class SessionAttendanceEntityFactory(
  session: SessionEntity? = null,
  groupMembership: ProgrammeGroupMembershipEntity? = null,
  recordedByFacilitator: FacilitatorEntity? = null,
) {
  private var id: UUID = UUID.randomUUID()
  private var session: SessionEntity = session ?: SessionFactory().produce()
  private var groupMembership: ProgrammeGroupMembershipEntity =
    groupMembership ?: ProgrammeGroupMembershipFactory().produce()
  private var legitimateAbsence: Boolean = true
  private var recordedByFacilitator: FacilitatorEntity = recordedByFacilitator ?: FacilitatorEntityFactory().produce()
  private var recordedAt: LocalDateTime? = LocalDateTime.now(ZoneId.of("UTC"))
  private var outcomeType: SessionAttendanceOutcomeTypeEntity =
    SessionAttendanceOutcomeTypeEntity(ATTC, "Attended - Complied", true, true)

  fun withId(id: UUID) = apply { this.id = id }
  fun withSession(session: SessionEntity) = apply { this.session = session }
  fun withGroupMembership(groupMembership: ProgrammeGroupMembershipEntity) = apply { this.groupMembership = groupMembership }

  fun withLegitimateAbsence(legitimateAbsence: Boolean) = apply { this.legitimateAbsence = legitimateAbsence }
  fun withRecordedByFacilitator(recordedByFacilitator: FacilitatorEntity) = apply { this.recordedByFacilitator = recordedByFacilitator }

  fun withRecordedAt(recordedAt: LocalDateTime) = apply { this.recordedAt = recordedAt }

  fun withOutcomeType(outcomeType: SessionAttendanceOutcomeTypeEntity) = apply { this.outcomeType = outcomeType }

  fun produce() = SessionAttendanceEntity(
    id = this.id,
    session = this.session,
    groupMembership = this.groupMembership,
    legitimateAbsence = this.legitimateAbsence,
    recordedByFacilitator = this.recordedByFacilitator,
    recordedAt = this.recordedAt,
    outcomeType = this.outcomeType,
  )
}
