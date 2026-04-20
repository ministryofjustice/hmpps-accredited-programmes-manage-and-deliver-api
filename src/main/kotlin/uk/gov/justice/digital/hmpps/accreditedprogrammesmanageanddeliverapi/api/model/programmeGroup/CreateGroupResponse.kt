package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Response returned when a programme group is created")
data class CreateGroupResponse(
  @get:JsonProperty("id", required = true)
  @Schema(description = "The id for the group")
  val id: UUID,

  @get:JsonProperty("successMessage", required = true)
  @Schema(description = "Success message for creating a group")
  val successMessage: String,
)
