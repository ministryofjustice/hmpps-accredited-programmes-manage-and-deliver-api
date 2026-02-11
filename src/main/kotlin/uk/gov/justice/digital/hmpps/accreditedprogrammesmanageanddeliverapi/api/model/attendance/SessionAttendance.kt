package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

@Schema(
  description = "Attendance for a session",
)
data class SessionAttendance(

  @NotEmpty(message = "attendees must not be empty")
  @get:JsonProperty(required = true)
  @Schema(description = "List of attendees for a session")
  var attendees: List<@Valid SessionAttendee>,

  @Schema(description = "Session attendance response message after processing")
  var responseMessage: String? = null,
)
