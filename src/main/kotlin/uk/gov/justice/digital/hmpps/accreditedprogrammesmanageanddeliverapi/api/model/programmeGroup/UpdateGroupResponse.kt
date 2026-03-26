package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response returned when a programme group is updated")
data class UpdateGroupResponse(
  @get:JsonProperty("successMessage")
  @Schema(description = "A message describing what was updated on the group")
  val successMessage: String,
)
