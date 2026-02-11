package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.UUID

@Schema(
  description = "Attendee of a session",
)
data class SessionAttendee(
  @NotNull(message = "attendeeId must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "Attendee ID of a session")
  var attendeeId: UUID,

  @NotBlank(message = "Name must not be blank")
  @get:JsonProperty(required = true)
  @Schema(description = "Name of the attendee of a session")
  var name: String,

  @NotNull(message = "attended must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "A flag showing if attendee attended a session")
  var attended: Boolean,

  @NotNull(message = "recordedAt must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "Date when a session was attended")
  @JsonFormat(pattern = "dd-MM-yyyy")
  @DateTimeFormat(pattern = "dd-MM-yyyy")
  var recordedAt: LocalDate,

  @NotNull(message = "recordedByFacilitatorId must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "ID of a session facilitator")
  var recordedByFacilitatorId: UUID,
)
