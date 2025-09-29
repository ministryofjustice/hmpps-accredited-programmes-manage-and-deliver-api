package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

data class CaseListFilters(
  @Schema(
    required = true,
    description = "Contains lists of open and closed referral statuses",
  )
  @get:JsonProperty("statusFilters", required = true)
  val statusFilters: StatusFilters,
)

data class StatusFilters(
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
