package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

data class RescheduleSessionRequest(
  @field:NotNull(message = "sessionStartDate must not be null")
  @get:JsonProperty("sessionStartDate", required = true)
  @Schema(description = "The start date of the session in YYYY-MM-DD format", example = "2025-12-17")
  @JsonFormat(pattern = "yyyy-MM-dd")
  val sessionStartDate: LocalDate,

  @Valid
  @NotNull(message = "sessionStartTime must not be null")
  @get:JsonProperty("sessionStartTime", required = true)
  @Schema(description = "The start time of the session")
  val sessionStartTime: SessionTime,

  @Valid
  @get:JsonProperty("sessionEndTime")
  @Schema(description = "The end time of the session")
  val sessionEndTime: SessionTime? = null,

  @NotNull(message = "rescheduleOtherSessions must not be null")
  @get:JsonProperty("rescheduleOtherSessions", required = true)
  @Schema(description = "Whether to reschedule other sessions", example = "true")
  val rescheduleOtherSessions: Boolean,
)
