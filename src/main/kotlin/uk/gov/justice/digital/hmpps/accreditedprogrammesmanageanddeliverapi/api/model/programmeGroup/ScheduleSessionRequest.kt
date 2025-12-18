package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.util.UUID

data class ScheduleSessionRequest(
  @NotNull(message = "sessionTemplateId must not be null")
  @get:JsonProperty("sessionTemplateId", required = true)
  @Schema(description = "The UUID of the ModuleSessionTemplate that defines the session blueprint")
  var sessionTemplateId: UUID,

  @NotNull(message = "referralIds must not be null")
  @NotEmpty(message = "referralIds must not be empty")
  @get:JsonProperty("referralIds", required = true)
  @Schema(description = "An array of Referral IDs representing the group members to schedule")
  var referralIds: List<UUID>,

  @Valid
  @NotNull(message = "facilitators must not be null")
  @NotEmpty(message = "facilitators must not be empty")
  @get:JsonProperty("facilitators", required = true)
  @Schema(description = "The facilitator(s) who will conduct the session")
  var facilitators: List<CreateGroupTeamMember>,

  @NotNull(message = "startDate must not be null")
  @get:JsonProperty("startDate", required = true)
  @Schema(description = "The start date of the session in YYYY-MM-DD format", example = "2025-12-17")
  @JsonFormat(pattern = "yyyy-MM-dd")
  var startDate: LocalDate,

  @Valid
  @NotNull(message = "startTime must not be null")
  @get:JsonProperty("startTime", required = true)
  @Schema(description = "The start time of the one-to-one session")
  var startTime: SessionTime,

  @Valid
  @NotNull(message = "endTime must not be null")
  @get:JsonProperty("endTime", required = true)
  @Schema(description = "The end time of the one-to-one session")
  var endTime: SessionTime,
)

data class SessionTime(
  @NotNull(message = "hour must not be null")
  @Min(value = 1, message = "hour must be between 1 and 12")
  @Max(value = 12, message = "hour must be between 1 and 12")
  @get:JsonProperty("hour", required = true)
  @Schema(description = "Hour in 12-hour format (1-12)", example = "10")
  var hour: Int,

  @NotNull(message = "minutes must not be null")
  @Min(value = 0, message = "minutes must be between 0 and 59")
  @Max(value = 59, message = "minutes must be between 0 and 59")
  @get:JsonProperty("minutes", required = true)
  @Schema(description = "Minutes (0-59)", example = "30")
  var minutes: Int,

  @NotNull(message = "amOrPm must not be null")
  @get:JsonProperty("amOrPm", required = true)
  @Schema(description = "AM or PM designation", implementation = AmOrPm::class)
  var amOrPm: AmOrPm,
)
