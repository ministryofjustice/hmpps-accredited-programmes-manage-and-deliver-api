package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Details of a Record Attendance")
data class RecordSessionAttendance(
  @Schema(
    description = "A title of a session",
    example = "Getting started 1",
  )
  val sessionTitle: String,

  @Schema(
    description = "Region name of a programme group",
    example = "North East",
  )
  val groupRegionName: String,

  @Schema(
    description = "List of attendees",
  )
  val people: List<SessionAttendancePerson>,
)

@Schema(description = "Details of an Attendee")
data class SessionAttendancePerson(
  @Schema(
    description = "A UUID string representing a referral ID",
    example = "cb64c21b-cf10-4a6d-a118-f61d4f27f47b",
  )
  val referralId: UUID,

  @Schema(
    description = "A string representing a full name of an attendee",
    example = "Alice Brown",
  )
  val name: String,

  @Schema(
    description = "A string representing a CRN",
    example = "Alice Brown",
  )
  val crn: String,

  @Schema(
    description = "A string an attendance",
    example = "Attended, failed to comply",
  )
  val attendance: String?,

  @Schema(
    description = "The session notes associated with the attendee",
    example = "Some session notes here",
  )
  val sessionNotes: String?,

  @Schema(
    description = "A list of options",
  )
  val options: List<Option>?,
)

@Schema(description = "Details of an Option")
data class Option(
  @Schema(
    description = "A string representing a text",
    example = "Attended",
  )
  val text: String?,

  @Schema(
    description = "A string representing a subtext",
    example = "Left early",
  )
  val subtext: String?,

  @Schema(
    description = "A string representing a value",
    example = "ndelius-outcome-1",
  )
  val value: String?,
)
