package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceOutcomeType
import java.util.UUID

@Schema(
  description = "Attendee of a session",
)
data class SessionAttendee(

  @NotNull(message = "referralId must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "The referral ID linked to the attendee")
  var referralId: UUID,

  @NotNull(message = "outcomeCode must not be null")
  @get:JsonProperty(required = true)
  @Schema(description = "The attendance outcome code for the attendee")
  var outcomeCode: SessionAttendanceOutcomeType,

  @Schema(description = "Session notes for the attendee")
  var sessionNotes: String? = null,
)
