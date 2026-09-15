package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import java.time.LocalDateTime
import java.util.UUID

data class SubjectAccessRequestSessionAttendance(
  val id: UUID?,
  val sessionId: UUID?,
  val groupMembershipId: UUID?,
  val outcomeType: SubjectAccessRequestSessionAttendanceNDeliusOutcome,
  val legitimateAbsence: String?,
  val noteHistory: MutableList<SubjectAccessRequestSessionNoteHistory>,
  val recordedByFacilitator: SubjectAccessRequestFacilitator?,
  val recordedAt: LocalDateTime?,
)

fun SessionAttendanceEntity.toApi() = SubjectAccessRequestSessionAttendance(
  id = id,
  sessionId = session.id,
  groupMembershipId = groupMembership.id,
  outcomeType = outcomeType.toApi(),
  legitimateAbsence = legitimateAbsence.toString(),
  noteHistory = notesHistory.map { it.toApi() }.toMutableList(),
  // APG-2580: use the surname-only projection here so the "Recorded by"
  // section of the SAR output matches the data-dictionary rule. The plain
  // `.toApi()` projection is retained on `FacilitatorEntity` for the
  // Facilitators-roster section, which keeps full names.
  recordedByFacilitator = recordedByFacilitator?.toSarRecordedByApi(),
  recordedAt = recordedAt,
)
