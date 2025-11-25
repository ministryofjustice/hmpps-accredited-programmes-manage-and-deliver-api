package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType

enum class FacilitatorType {
  @JsonProperty("LEAD_FACILITATOR")
  LEAD_FACILITATOR,

  @JsonProperty("REGULAR_FACILITATOR")
  REGULAR_FACILITATOR,

  @JsonProperty("COVER_FACILITATOR")
  COVER_FACILITATOR,
}

fun CreateGroupTeamMemberType.toFacilitatorType(): FacilitatorType = when (this) {
  CreateGroupTeamMemberType.LEAD_FACILITATOR -> FacilitatorType.LEAD_FACILITATOR
  CreateGroupTeamMemberType.REGULAR_FACILITATOR -> FacilitatorType.REGULAR_FACILITATOR
  CreateGroupTeamMemberType.COVER_FACILITATOR -> FacilitatorType.COVER_FACILITATOR
  CreateGroupTeamMemberType.TREATMENT_MANAGER -> throw IllegalArgumentException("TREATMENT_MANAGER cannot be converted to FacilitatorType")
}
