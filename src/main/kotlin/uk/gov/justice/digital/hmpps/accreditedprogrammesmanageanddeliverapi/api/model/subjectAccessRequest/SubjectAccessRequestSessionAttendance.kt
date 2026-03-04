package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.subjectAccessRequest

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity

data class SubjectAccessRequestSessionAttendance(
  val outcomeType: SubjectAccessRequestSessionAttendanceNDeliusOutcome,
  val legitimateAbsence: Boolean?,
  val noteHistory: MutableList<SubjectAccessRequestSessionNoteHistory>,
  var recordedByFacilitator: SubjectAccessRequestFacilitator?,
)

fun SessionAttendanceEntity.toApi() = SubjectAccessRequestSessionAttendance(
  outcomeType = outcomeType.toApi(),
  legitimateAbsence = legitimateAbsence,
  noteHistory = notesHistory.map { it.toApi() }.toMutableList(),
  recordedByFacilitator = recordedByFacilitator?.toApi(),
)
