package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class GroupMember(
  @get:JsonProperty("name", required = true)
  @Schema(description = "The full name of the group member as 'firstname lastname'", example = "John Doe")
  val name: String,

  @get:JsonProperty("crn", required = true)
  @Schema(description = "The Case Reference Number of the group member")
  val crn: String,

  @get:JsonProperty("referralId", required = true)
  @Schema(description = "The UUID of the referral for this group member")
  val referralId: UUID,
)
