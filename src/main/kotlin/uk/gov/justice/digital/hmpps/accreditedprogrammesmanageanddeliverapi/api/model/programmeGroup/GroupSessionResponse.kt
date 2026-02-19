package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class GroupSessionResponse(
  @Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val groupCode: String,

  @Schema(description = "The title of the page", required = true, example = "Attendance and notes for Getting started session")
  val pageTitle: String,

  @Schema(description = "The type of session", required = true, example = "one-to-one")
  val sessionType: String,

  @Schema(description = "The date of the session", required = true, example = "Thursday 12 January 2023")
  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  val date: LocalDate,

  @Schema(description = "The time of the session", required = true, example = "11am")
  val time: String,

  @Schema(description = "The list of people scheduled to attend", required = true, example = "[John Smith, Jane Doe]")
  val scheduledToAttend: List<String>,

  @Schema(description = "The names of the facilitators in the session", required = true, example = "[John Doe, Jane Smith]")
  val facilitators: List<String>,

  @Schema(description = "The attendance and session notes for each attendee", required = true)
  val attendanceAndSessionNotes: List<AttendanceAndSessionNotes>,
)

data class AttendanceAndSessionNotes(
  val name: String,
  val referralId: UUID,
  val crn: String,
  val attendance: String,
  val sessionNotes: String,
)
