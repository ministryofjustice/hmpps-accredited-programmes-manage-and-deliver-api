package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.session

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember

@Schema(
  description = "Response representing attendees for a specific programme session",
)
data class EditSessionFacilitatorsResponse(
  val headingText: String,
  val facilitators: List<EditSessionFacilitator>,
) {
  data class EditSessionFacilitator(
    @NotNull(message = "facilitator must not be null")
    @get:JsonProperty("facilitator", required = true)
    @Schema(description = "The full name of the facilitator for the group")
    var facilitatorName: String,

    @NotNull(message = "facilitatorCode must not be null")
    @get:JsonProperty("facilitatorCode", required = true)
    @Schema(description = "The code of the facilitator for the group")
    var facilitatorCode: String,

    @NotNull
    @get:JsonProperty("teamName", required = true)
    @Schema(description = "The name of the team that the member belongs to")
    var teamName: String,

    @NotNull
    @get:JsonProperty("teamCode", required = true)
    @Schema(description = "The code of the team that the member belongs to")
    var teamCode: String,

    @NotNull
    @get:JsonProperty("currentlyFacilitating", required = true)
    @Schema(description = "Is the facilitator currently scheduled on this session")
    var currentlyFacilitating: Boolean,
  )
}

fun UserTeamMember.toEditSessionFacilitator(sessionFacilitatorCodes: List<String>) = EditSessionFacilitatorsResponse.EditSessionFacilitator(
  facilitatorName = personName,
  facilitatorCode = personCode,
  teamName = teamName,
  teamCode = teamCode,
  currentlyFacilitating = sessionFacilitatorCodes.contains(personCode),
)
