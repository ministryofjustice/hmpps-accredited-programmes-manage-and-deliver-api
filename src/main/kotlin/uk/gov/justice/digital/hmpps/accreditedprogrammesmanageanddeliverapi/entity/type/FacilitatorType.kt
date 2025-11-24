package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type

import com.fasterxml.jackson.annotation.JsonProperty

enum class FacilitatorType {
  @JsonProperty("lead_facilitator")
  LEAD_FACILITATOR,

  @JsonProperty("regular_facilitator")
  REGULAR_FACILITATOR,

  @JsonProperty("cover_facilitator")
  COVER_FACILITATOR,
}
