package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonName
import java.time.LocalDate
import java.util.UUID

data class ProgrammeGroupModuleSessionsResponse(
  @get:JsonProperty("group", required = true)
  @field:Schema(description = "group details")
  val group: ProgrammeGroupModuleSessionsResponseGroup,

  @get:JsonProperty("modules", required = true)
  @field:Schema(description = "Details of the Group's modules")
  val modules: List<ProgrammeGroupModuleSessionsResponseGroupModule>,
)

data class ProgrammeGroupModuleSessionsResponseGroup(
  @field:Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val code: String,

  @field:Schema(
    example = "West Midlands",
    required = true,
    description = "The region name the group belongs to.",
  )
  @get:JsonProperty("regionName", required = true)
  val regionName: String,
)

data class ProgrammeGroupModuleSessionsResponseGroupModule(
  @field:Schema(
    description = "The unique identifier of the module",
    required = true,
    example = "123e4567-e89b-12d3-a456-426614174000",
  )
  val id: UUID,

  @field:Schema(description = "The module number", required = true, example = "1")
  val number: Int,

  @field:Schema(description = "The name of the module", required = true, example = "Getting started")
  val name: String,

  @field:Schema(
    description = "Object containing the start date text",
    required = true,
    example = "Estimated date of Getting started one-to-ones: Thursday 21 May 2026",
  )
  val startDateText: StartDateText,

  @field:Schema(
    description = "The text to display on the schedule button",
    required = true,
    example = "Schedule a Getting started session",
  )
  val scheduleButtonText: String,

  @field:Schema(description = "The sessions within the module", required = true)
  val sessions: List<ProgrammeGroupModuleSessionsResponseGroupSession>,
)

data class ProgrammeGroupModuleSessionsResponseGroupSession(
  @field:Schema(
    description = "The unique identifier of the session template",
    required = true,
    example = "123e4567-e89b-12d3-a456-426614174000",
  )
  val id: UUID,

  @field:Schema(description = "The sequential number of the session within its module", required = true, example = "1")
  val number: Int,

  @field:Schema(
    description = "The display name of the session",
    required = true,
    example = "Getting started one-to-one",
  )
  val name: String,

  @field:Schema(description = "The type of session", required = true, example = "one-to-one")
  val type: String,

  @field:Schema(description = "Whether the session is a catch-up session", required = true, example = "false")
  val isCatchup: Boolean,

  @field:Schema(description = "The date of the session", required = true, example = "Thursday 12 January 2023")
  @get:JsonFormat(pattern = "EEEE d MMMM yyyy")
  val dateOfSession: LocalDate,

  @field:Schema(description = "The time of the session", required = true, example = "11am")
  val timeOfSession: String,

  @field:Schema(
    description = "The time of the session with special times capitalised",
    required = true,
    example = "11am to Midday",
  )
  val timeWithCapitalisedMidday: String = "",

  @field:Schema(description = "The list of participants in the session", required = true)
  val participants: List<Participant>,

  @field:Schema(
    description = "The names of the facilitators in the session",
    required = true,
    example = "[\"John Doe\", \"Jane Smith\"]",
  )
  val facilitators: List<String>,
)

data class StartDateText(
  @field:Schema(
    description = "The bold estimated date text on the ui",
    required = true,
    example = "Estimated start date of pre-group one-to-ones",
  )
  val estimatedStartDateText: String,

  @field:Schema(description = "The date of the earliest session", required = true, example = "Thursday 12 January 2023")
  val sessionStartDate: String,
)

data class Participant(
  @field:Schema(description = "The name of the participant", required = true)
  val name: PersonName,
  @field:Schema(description = "The CRN of the participant", example = "X12345")
  val crn: String? = null,
  @field:Schema(description = "A flag denoting whether the person is a limited access offender", example = "true")
  val isLimitedAccessOffender: Boolean? = false,
  @field:Schema(
    description = "A flag denoting whether the logged in user is excluded from viewing this person",
    example = "false",
  )
  val isExcluded: Boolean? = false,
)
