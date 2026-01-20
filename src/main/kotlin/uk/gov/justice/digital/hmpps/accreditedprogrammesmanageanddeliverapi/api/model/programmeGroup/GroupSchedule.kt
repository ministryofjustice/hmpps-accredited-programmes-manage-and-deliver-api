package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class GroupSchedule(
  @get:JsonProperty("preGroupOneToOneStartDate", required = true)
  @Schema(description = "The start date of a group one to one in format DayName DateNumber MonthName YearNumber", example = "Monday 22 June 2026")
  @JsonFormat(pattern = "d MMMM yyyy")
  val preGroupOneToOneStartDate: String,

  @get:JsonProperty("gettingStartedModuleStartDate", required = true)
  @Schema(description = "The start date of a module in format DayName DateNumber MonthName YearNumber", example = "Monday 22 June 2026")
  @JsonFormat(pattern = "d MMMM yyyy")
  val gettingStartedModuleStartDate: String,

  @get:JsonProperty("endDate", required = true)
  @JsonFormat(pattern = "d MMMM yyyy")
  @Schema(description = "The end date of a module in format DayName DateNumber MonthName YearNumber", example = "Monday 22 September 2026")
  val endDate: String,

  @get:JsonProperty("sessions", required = true)
  @Schema(description = "Details of the Group's sessions")
  val modules: List<GroupScheduleSession>,
)

data class GroupScheduleSession(

  @Schema(description = "id of the session", required = true, example = "UUID")
  val id: UUID?,

  @Schema(description = "The name of the session", required = true, example = "Pre Group one-to-ones")
  val name: String,

  @Schema(description = "The type of the session", required = true, example = "Individual")
  val type: String,

  @JsonFormat(pattern = "d MMMM yyyy")
  @Schema(description = "The date of the session", example = "Monday 22 June 2026")
  val date: String,

  @Schema(description = "The time(s) of the session. For example 11am to 1:30pm or Various times", required = true, example = "Various times")
  val time: String,
)
