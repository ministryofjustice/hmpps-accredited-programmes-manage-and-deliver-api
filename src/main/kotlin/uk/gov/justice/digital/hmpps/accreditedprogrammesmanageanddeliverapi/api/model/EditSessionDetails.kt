package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import java.util.UUID

data class EditSessionDetails(
  val sessionId: UUID,
  val groupCode: String,
  val sessionName: String,
  val sessionDate: String,
  val sessionStartTime: SessionTime,
  val sessionEndTime: SessionTime,
  @get:JsonProperty("isEmptyGroup", required = true)
  @Schema(
    description = "True when the group has never had any membership. When true the UI may reschedule even if the submitted date is in the past.",
    example = "false",
  )
  val isEmptyGroup: Boolean = false,
)
