package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.update

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import java.util.UUID

data class UpdateAvailability(

  @get:JsonProperty("availabilityId", required = true)
  @Schema(example = "d3f55f38-7c7b-4b6e-9aa1-e7d7f9e3e7893", description = "The ID of the availability to update", required = true)
  val availabilityId: UUID,

  @get:JsonProperty("referralId", required = true)
  @Schema(example = "d3f55f38-7c7b-4b6e-9aa1-e7d7f9e3e785", description = "The ID of the referral", required = true)
  val referralId: UUID,

  @get:JsonProperty("startDate")
  @Schema(example = "2025-07-10", description = "Start date of the availability, Start date of the availability, will default to current date if no value is passed in")
  val startDate: String? = null,

  @get:JsonProperty("endDate")
  @Schema(example = "2025-07-20", description = "End date of the availability", nullable = true)
  val endDate: String? = null,

  @get:JsonProperty("otherDetails")
  @Schema(example = "Available for remote sessions", description = "Additional details", nullable = true)
  val otherDetails: String? = null,

  @JsonProperty("availabilities")
  val availabilities: List<DailyAvailabilityModel>,
)
