package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PersonName

data class UserTeamMember(
  @get:JsonProperty("personCode", required = true)
  @field:Schema(description = "The code for the team member")
  val personCode: String,

  @get:JsonProperty("personName", required = true)
  @field:Schema(description = "The full name of the team member")
  val personName: PersonName,

  @get:JsonProperty("teamName", required = true)
  @field:Schema(description = "The name of the team that the member belongs to")
  val teamName: String,

  @get:JsonProperty("teamCode", required = true)
  @field:Schema(description = "The code of the team that the member belongs to")
  val teamCode: String,

)
