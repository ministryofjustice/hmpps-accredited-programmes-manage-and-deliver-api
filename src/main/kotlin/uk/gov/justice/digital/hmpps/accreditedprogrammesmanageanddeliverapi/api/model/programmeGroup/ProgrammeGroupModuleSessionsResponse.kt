package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import java.util.UUID

data class ProgrammeGroupModuleSessionsResponse(
  @get:JsonProperty("group", required = true)
  @Schema(description = "group details")
  val group: ProgrammeGroupModuleSessionsResponseGroup,

  @get:JsonProperty("modules", required = true)
  @Schema(description = "Details of the Group's modules")
  val modules: List<ProgrammeGroupModuleSessionsResponseGroupModule>,
)

data class ProgrammeGroupModuleSessionsResponseGroup(
  @Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val code: String,

  @Schema(
    example = "West Midlands",
    required = true,
    description = "The region name the group belongs to.",
  )
  @get:JsonProperty("regionName", required = true)
  val regionName: String,
)

data class ProgrammeGroupModuleSessionsResponseGroupModule(
  @Schema(description = "The unique identifier of the module", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
  val id: UUID,

  @Schema(description = "The module number", required = true, example = "1")
  val number: Int,

  @Schema(description = "The name of the module", required = true, example = "Getting started")
  val name: String,

  @Schema(description = "Object containing the start date text", required = true, example = "Estimated date of Getting started one-to-ones: Thursday 21 May 2026")
  val startDateText: StartDateText,

  @Schema(description = "The text to display on the schedule button", required = true, example = "Schedule a Getting started session")
  val scheduleButtonText: String,

  @Schema(description = "The sessions within the module", required = true)
  val sessions: List<ProgrammeGroupModuleSessionsResponseGroupSession>,
)

data class ProgrammeGroupModuleSessionsResponseGroupSession(
  @Schema(description = "The unique identifier of the session template", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
  val id: UUID,

  @Schema(description = "The sequential number of the session within its module", required = true, example = "1")
  val number: Int,

  @Schema(description = "The display name of the session", required = true, example = "Getting started one-to-one")
  val name: String,

  @Schema(description = "The type of session", required = true, example = "one-to-one")
  val type: SessionType,

  @Schema(description = "The date of the session", required = true, example = "Thursday 12 January 2023")
  val dateOfSession: String,

  @Schema(description = "The time of the session", required = true, example = "11am")
  val timeOfSession: String,

  @Schema(description = "The names of the participants in the session", required = true, example = "[John Doe, Jane Smith]")
  val participants: List<String>,

  @Schema(description = "The names of the facilitators in the session", required = true, example = "[John Doe, Jane Smith]")
  val facilitators: List<String>,
)

data class StartDateText(
  @Schema(description = "The bold estimated date text on the ui", required = true, example = "Estimated start date of pre-group one-to-ones")
  val sessionStartDate: String,

  @Schema(description = "The date of the earliest session", required = true, example = "Thursday 12 January 2023")
  val body: String,
)
