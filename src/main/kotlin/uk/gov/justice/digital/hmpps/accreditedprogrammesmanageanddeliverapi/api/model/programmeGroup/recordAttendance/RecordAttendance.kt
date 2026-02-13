package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.recordAttendance

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of a Record Attendance")
data class RecordAttendance(
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
  val people: List<Attendee>,
)

@Schema(description = "Details of an Attendee")
data class Attendee(
  @Schema(
    description = "A UUID string representing a referral ID",
    example = "cb64c21b-cf10-4a6d-a118-f61d4f27f47b",
  )
  val referralId: String,

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
