package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom

data class ReferralSentenceReferenceRequest(
  @get:JsonProperty("sourcedFrom", required = true)
  @Schema(example = "REQUIREMENT", description = "Source from of the referral", required = true)
  val sourcedFrom: ReferralEntitySourcedFrom,

  @get:JsonProperty("eventId", required = true)
  @Schema(example = "2500828798", description = "Event ID of the referral", required = true)
  val eventId: String,
)
