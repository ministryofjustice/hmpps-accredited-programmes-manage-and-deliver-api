package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

data class GroupMember(
  @get:JsonProperty("name", required = true)
  @get:Schema(description = "The full name of the group member as 'firstname lastname'", example = "John Doe")
  val name: String,

  @get:JsonProperty("crn", required = true)
  @get:Schema(description = "The Case Reference Number of the group member")
  val crn: String,

  @get:JsonProperty("referralId", required = true)
  @get:Schema(description = "The UUID of the referral for this group member")
  val referralId: UUID,

  @get:JsonProperty("isLimitedAccessOffender")
  @set:JsonProperty("isLimitedAccessOffender")
  @get:Schema(description = "The boolean value of whether the group member has Limited Access Offender (LAO) status")
  var isLimitedAccessOffender: Boolean? = false,

  @get:JsonProperty("isExcluded")
  @set:JsonProperty("isExcluded")
  @get:Schema(description = "The boolean value of whether the group member details are excluded from viewing by the logged-in username")
  var isExcluded: Boolean? = false,
)
