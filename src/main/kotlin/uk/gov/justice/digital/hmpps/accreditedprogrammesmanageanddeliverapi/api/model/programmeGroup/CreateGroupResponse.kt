package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Response returned when a programme group is created")
data class CreateGroupResponse(
  @get:JsonProperty("id", required = true)
  @Schema(description = "The id for the group")
  var id: UUID,

  @get:JsonProperty("successMessage")
  @Schema(description = "A message describing what was updated on the group")
  val successMessage: String,
)
