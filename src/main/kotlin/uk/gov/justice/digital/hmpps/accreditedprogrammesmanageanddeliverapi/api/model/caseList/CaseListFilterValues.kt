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
)

data class StatusFilterValues(
  @Schema(
    required = true,
    description = "Open referral statuses",
    examples = ["Awaiting assessment", "Awaiting allocation"],
  )
  @get:JsonProperty("open", required = true)
  val open: List<StatusFilterItems>,
  @Schema(
    required = true,
    description = "Closed referral statuses",
    examples = ["Programme complete", "Withdrawn"],
  )
  @get:JsonProperty("closed", required = true)
  val closed: List<StatusFilterItems>,
)

data class StatusFilterItems(
  @Schema(
    required = true,
    description = "Display value for the status filter",
    example = "Programme complete",
  )
  @get:JsonProperty("text", required = true)
  val text: String,
  @Schema(
    required = true,
    description = "UTF-8 encoded value for the status filter",
    example = "Programme+complete",
  )
  @get:JsonProperty("value", required = true)
  val value: String,
)
