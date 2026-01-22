package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import java.time.LocalDate
import java.time.LocalTime

data class GroupSessionResponse(
  val groupCode: String,
  val pageTitle: String,
  val sessionType: String,
  val date: LocalDate,
  val time: LocalTime,
  val scheduledToAttend: List<String>,
  val facilitators: List<String>,
  val attendanceAndSessionNotes: List<AttendanceAndSessionNotes>,
) {

  data class AttendanceAndSessionNotes(
    val name: String,
    val crn: String,
    // TODO Implement this when we have attendance
    val attendance: String,
    // TODO Implement this when we have session notes
    val sessionNotes: String,
  )
}
