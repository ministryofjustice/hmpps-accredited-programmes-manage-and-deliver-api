package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class UserTeamMember(
  @get:JsonProperty("code", required = true)
  @Schema(description = "The code for the team member")
  val code: String,
  @get:JsonProperty("name", required = true)
  @Schema(description = "The full name of the team member")
  val name: String,
)
