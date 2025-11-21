package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class UserTeamMember(
  @get:JsonProperty("personCode", required = true)
  @Schema(description = "The code for the team member")
  val personCode: String,

  @get:JsonProperty("personName", required = true)
  @Schema(description = "The full name of the team member")
  val personName: String,

  @get:JsonProperty("teamName", required = true)
  @Schema(description = "The name of the team that the member belongs to")
  val teamName: String,

  @get:JsonProperty("teamCode", required = true)
  @Schema(description = "The code of the team that the member belongs to")
  val teamCode: String,

)
