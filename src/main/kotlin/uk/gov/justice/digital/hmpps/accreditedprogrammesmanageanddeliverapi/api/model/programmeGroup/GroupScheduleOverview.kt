package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.util.UUID

data class GroupScheduleOverview(
  @get:JsonProperty("preGroupOneToOneStartDate", required = true)
  @Schema(
    description = "The start date of a group one to one in format DayName DateNumber MonthName YearNumber",
    example = "Monday 22 June 2026",
  )
  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  val preGroupOneToOneStartDate: LocalDate?,

  @get:JsonProperty("gettingStartedModuleStartDate", required = true)
  @Schema(
    description = "The start date of a module in format DayName DateNumber MonthName YearNumber",
    example = "Monday 22 June 2026",
  )
  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  val gettingStartedModuleStartDate: LocalDate?,

  @get:JsonProperty("endDate", required = true)
  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  @Schema(
    description = "The end date of a module in format DayName DateNumber MonthName YearNumber",
    example = "Monday 22 September 2026",
  )
  val endDate: LocalDate?,

  @get:JsonProperty("sessions", required = true)
  @Schema(description = "Details of the Group's sessions")
  val sessions: List<GroupScheduleOverviewSession>,

  @Schema(
    example = "AP_BIRMINGHAM_NORTH",
    required = true,
    description = "A unique code identifying the programme group.",
  )
  @get:JsonProperty("code", required = true)
  val groupCode: String,
)

data class GroupScheduleOverviewSession(

  @Schema(description = "id of the session", required = true, example = "UUID")
  val id: UUID?,

  @Schema(description = "The name of the session", required = true, example = "Pre Group one-to-ones")
  val name: String,

  @Schema(description = "The type of the session", required = true, example = "Individual")
  val type: String,

  @JsonFormat(pattern = "EEEE d MMMM yyyy")
  @Schema(description = "The date of the session", example = "Monday 22 June 2026")
  val date: LocalDate,

  @Schema(
    description = "The time(s) of the session. For example 11am to 1:30pm or Various times",
    required = true,
    example = "Various times",
  )
  val time: String,
)
