package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Details for rescheduling a session")
data class RescheduleSessionDetails(
  @Schema(description = "The unique session identifier", example = "cc4114d2-d27f-449e-8c31-645366432b49")
  val sessionId: UUID,
  @Schema(description = "The name of the session", example = "Edit Session 1")
  val sessionName: String,
  @Schema(description = "The previous date and time of the session", example = "Thursday 21 May 2026, 11am to 1:30pm")
  val previousSessionDateAndTime: String,
)
