package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

data class GroupsByRegion(
  @Schema(
    required = true,
    description = "Paged data containing the list of groups",
  )
  @get:JsonProperty("pagedGroupData", required = true)
  val pagedGroupData: Page<Group>,

  @Schema(
    example = "12",
    required = true,
    description = "The total number of records in the 'Other' tab.",
  )
  @get:JsonProperty("otherTabTotal", required = true)
  val otherTabTotal: Int,
)
