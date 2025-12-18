package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type

import com.fasterxml.jackson.annotation.JsonProperty

enum class CreateGroupTeamMemberType {
  @JsonProperty("TREATMENT_MANAGER")
  TREATMENT_MANAGER,

  @JsonProperty("LEAD_FACILITATOR")
  LEAD_FACILITATOR,

  @JsonProperty("REGULAR_FACILITATOR")
  REGULAR_FACILITATOR,

  @JsonProperty("COVER_FACILITATOR")
  COVER_FACILITATOR,
}
