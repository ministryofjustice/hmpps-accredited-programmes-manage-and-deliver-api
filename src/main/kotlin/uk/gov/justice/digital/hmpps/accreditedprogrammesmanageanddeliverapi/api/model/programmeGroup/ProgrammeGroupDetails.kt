package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Details of a Programme Group including filters and paginated group data.")
data class ProgrammeGroupDetails(

  @Schema(
    required = true,
    description = "Details of the group such as its code and regional name.",
  )
  @get:JsonProperty("group", required = true)
  val group: Group,

  @Schema(
    required = true,
    description = "Filter options available for the group data.",
  )
  @get:JsonProperty("filters", required = true)
  val filters: Filters,

  @Schema(
    required = true,
    description = "Paged data containing the list of group items (referrals or people) for this group.",
  )
  @get:JsonProperty("pagedGroupData", required = true)
  val pagedGroupData: Page<GroupItem>,

  @Schema(
    example = "12",
    required = true,
    description = "The total number of records in the 'Other' tab.",
  )
  @get:JsonProperty("otherTabTotal", required = true)
  val otherTabTotal: Int,
) {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Information identifying the group.")
  data class Group(
    @Schema(
      example = "AP_BIRMINGHAM_NORTH",
      required = true,
      description = "A unique code identifying the programme group.",
    )
    @get:JsonProperty("code", required = true)
    val code: String,

    @Schema(
      example = "West Midlands",
      required = true,
      description = "The region name the group belongs to.",
    )
    @get:JsonProperty("regionName", required = true)
    val regionName: String,
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Schema(description = "Available filter options for viewing programme group data.")
  data class Filters(

    @Schema(
      example = "[\"Male\", \"Female\"]",
      required = true,
      description = "The available sex options that can be used to filter the data.",
    )
    @get:JsonProperty("sex", required = true)
    val sex: List<String> = listOf("Male", "Female"),

    @Schema(
      example = "[\"General\", \"Sexual\", \"Domestic Violence\"]",
      required = true,
      description = "The available cohorts (offence types or programme categories) that can be used for filtering.",
    )
    @get:JsonProperty("cohort", required = true)
    val cohort: List<String> = ProgrammeGroupCohort.entries.map { it.label },

    @Schema(
      example = "[\"Durham\", \"Leeds\", \"Manchester\"]",
      required = false,
      description = "The list of PDU (Probation Delivery Unit) names that can be used for filtering.",
    )
    @get:JsonProperty("pduNames", required = false)
    val pduNames: List<String> = emptyList(),

    @Schema(
      example = "[\"Durham Team 1\", \"Durham Team 2\"]",
      required = false,
      description = "The list of reporting teams that can be used for filtering.",
    )
    @get:JsonProperty("reportingTeams", required = false)
    val reportingTeams: List<String> = emptyList(),
  )
}
