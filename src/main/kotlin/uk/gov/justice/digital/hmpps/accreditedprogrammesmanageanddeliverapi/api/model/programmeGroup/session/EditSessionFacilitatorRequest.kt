package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity

@Schema(
  description = "Request to edit the facilitators for a session",
)
data class EditSessionFacilitatorRequest(
  @NotNull(message = "facilitatorName must not be null")
  @NotBlank(message = "facilitatorName must not be blank")
  @get:JsonProperty("facilitator", required = true)
  @Schema(description = "The full name of the facilitator for the group")
  var facilitatorName: String,

  @NotNull(message = "facilitatorCode must not be null")
  @NotBlank(message = "facilitatorCode must not be blank")
  @get:JsonProperty("facilitatorCode", required = true)
  @Schema(description = "The code of the facilitator for the group")
  var facilitatorCode: String,

  @NotNull(message = "teamName must not be null")
  @NotBlank(message = "teamName must not be blank")
  @get:JsonProperty("teamName", required = true)
  @Schema(description = "The name of the team that the member belongs to")
  var teamName: String,

  @NotNull(message = "teamCode must not be null")
  @NotBlank(message = "teamCode must not be blank")
  @get:JsonProperty("teamCode", required = true)
  @Schema(description = "The code of the team that the member belongs to")
  var teamCode: String,
)

fun EditSessionFacilitatorRequest.toFacilitatorEntity(): FacilitatorEntity = FacilitatorEntity(
  personName = facilitatorName,
  ndeliusPersonCode = facilitatorCode,
  ndeliusTeamCode = teamCode,
  ndeliusTeamName = teamName,
)
