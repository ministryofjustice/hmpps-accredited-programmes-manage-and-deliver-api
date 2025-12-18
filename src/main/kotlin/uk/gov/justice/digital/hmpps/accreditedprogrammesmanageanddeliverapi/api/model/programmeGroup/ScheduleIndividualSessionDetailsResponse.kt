package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class ScheduleIndividualSessionDetailsResponse(
  @get:JsonProperty("facilitators", required = true)
  @Schema(description = "List of facilitators available for the one-to-one appointment (sourced from the Region of the logged-in user)")
  val facilitators: List<UserTeamMember>,

  @get:JsonProperty("groupMembers", required = true)
  @Schema(description = "Details of the Group's members via their Referrals")
  val groupMembers: List<GroupMember>,
)
