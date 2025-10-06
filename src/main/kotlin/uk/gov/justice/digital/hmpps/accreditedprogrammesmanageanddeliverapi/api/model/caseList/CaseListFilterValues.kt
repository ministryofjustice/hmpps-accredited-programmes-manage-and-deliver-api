package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CaseListFilterValues(
  @Schema(
    required = true,
    description = "Contains lists of open and closed referral statuses",
  )
  @get:JsonProperty("statusFilters", required = true)
  val statusFilterValues: StatusFilterValues,

  @Schema(
    required = true,
    description = "Contains pdu's with a list of their reporting teams",
  )
  @get:JsonProperty("locationFilters", required = true)
  val locationFilterValues: List<LocationFilterValues>,

  @Schema(
    required = true,
    description = "A count of the referrals for the opposite caselist tab you are in",
  )
  @get:JsonProperty("otherReferralsCount", required = true)
  val otherReferralsCount: Int,
)

data class StatusFilterValues(
  @Schema(
    required = true,
    description = "Open referral statuses",
    examples = ["Awaiting assessment", "Awaiting allocation"],
  )
  @get:JsonProperty("open", required = true)
  val open: List<String>,
  @Schema(
    required = true,
    description = "Closed referral statuses",
    examples = ["Programme complete", "Withdrawn"],
  )
  @get:JsonProperty("closed", required = true)
  val closed: List<String>,
)

data class LocationFilterValues(
  @Schema(
    required = true,
    description = "The name of a pdu",
    examples = ["London"],
  )
  @get:JsonProperty("pduName", required = true)
  val pduName: String,
  @Schema(
    required = true,
    description = "List of the reporting teams for a specific pdu",
    examples = ["Team 1", "Team 2"],
  )
  @get:JsonProperty("reportingTeams", required = true)
  val reportingTeams: List<String>,
)

data class PduReportingLocation(
  val pduName: String,
  val reportingTeam: String,
)
