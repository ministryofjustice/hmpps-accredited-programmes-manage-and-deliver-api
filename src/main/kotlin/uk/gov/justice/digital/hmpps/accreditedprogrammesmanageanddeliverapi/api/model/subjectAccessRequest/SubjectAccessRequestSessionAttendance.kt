package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionAttendance(
  val id: UUID?,
  val sessionId: UUID?,
  val groupMembershipId: UUID?,
  val outcomeType: SubjectAccessRequestSessionAttendanceNDeliusOutcome,
  val legitimateAbsence: Boolean?,
  val noteHistory: MutableList<SubjectAccessRequestSessionNoteHistory>,
  val recordedByFacilitator: SubjectAccessRequestFacilitator?,
  val recordedAt: LocalDateTime?,
  val createdBy: String,
  val createdAt: LocalDateTime,
)

fun SessionAttendanceEntity.toApi() = SubjectAccessRequestSessionAttendance(
  id = id,
  sessionId = session.id,
  groupMembershipId = groupMembership.id,
  outcomeType = outcomeType.toApi(),
  legitimateAbsence = legitimateAbsence,
  noteHistory = notesHistory.map { it.toApi() }.toMutableList(),
  recordedByFacilitator = recordedByFacilitator?.toApi(),
  recordedAt = recordedAt,
  createdBy = createdBy,
  createdAt = createdAt,
)
