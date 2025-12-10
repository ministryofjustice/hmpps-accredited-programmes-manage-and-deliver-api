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
    description = "The total number of records in the other tab, such as the Not started tab.",
  )
  @get:JsonProperty("otherTabTotal", required = true)
  val otherTabTotal: Int,

  @Schema(
    required = true,
    description = "A list of (unique) Probation Delivery Units names across all of the Groups",
  )
  @get:JsonProperty("probationDeliveryUnitNames", required = true)
  val probationDeliveryUnitNames: List<String>,

  @Schema(
    required = false,
    description = "A list of (unique) Delivery Locations for Referrals within the current PDU.  Only present if a probationDeliveryUnit is specified",
  )
  @get:JsonProperty("deliveryLocationNames", required = false)
  val deliveryLocationNames: List<String>?,

  @Schema(
    example = "West Midlands",
    required = true,
    description = "The region name the groups belongs to.",
  )
  @get:JsonProperty("regionName", required = true)
  val regionName: String,
)
