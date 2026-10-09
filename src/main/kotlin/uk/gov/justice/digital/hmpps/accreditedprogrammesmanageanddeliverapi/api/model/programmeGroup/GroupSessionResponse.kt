package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonName
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class GroupSessionResponse(
  @field:Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val groupCode: String,

  @field:Schema(
    description = "The title of the page",
    required = true,
    example = "Attendance and notes for Getting started session",
  )
  val pageTitle: String,

  @field:Schema(description = "The type of session", required = true, example = "one-to-one")
  val sessionType: String,

  @field:Schema(
    description = "Indicates whether this is a catch-up session",
    example = "false",
  )
  val isCatchup: Boolean,

  @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  @field:Schema(
    description = "The unformatted end date and time of the session for sorting",
  )
  val unformattedEndDate: LocalDateTime,

  @field:Schema(description = "The date of the session", required = true, example = "Thursday 12 January 2023")
  @get:JsonFormat(pattern = "EEEE d MMMM yyyy")
  val date: LocalDate,

  @field:Schema(description = "The time of the session", required = true, example = "11am")
  val time: String,

  @field:Schema(
    description = "The time of the session with special times capitalised",
    required = true,
    example = "Midday to 1pm",
  )
  val timeWithCapitalisedMidday: String = "",

  @field:Schema(description = "The list of people scheduled to attend", required = true)
  val scheduledToAttend: List<PersonName>,

  @field:Schema(
    description = "The names of the facilitators in the session",
    required = true,
    example = "[John Doe, Jane Smith]",
  )
  val facilitators: List<String>,

  @field:Schema(description = "The attendance and session notes for each attendee", required = true)
  val attendanceAndSessionNotes: List<AttendanceAndSessionNotes>,
)

data class AttendanceAndSessionNotes(
  @field:Schema(description = "The name of the person that attended a session", required = true)
  val name: PersonName,

  val referralId: UUID,
  val crn: String,
  val lao: Boolean,

  @field:Schema(
    description = "True when the current user is not authorised to view this Limited Access Offender.",
    example = "false",
  )
  val isExcluded: Boolean = false,

  val attendance: String,
  val sessionNotes: String,
)
