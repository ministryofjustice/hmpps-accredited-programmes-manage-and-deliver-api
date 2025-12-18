package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails

// This required to map our Page<> Concrete type so that jackson can deserialize when checking our api responses.
@JsonIgnoreProperties(ignoreUnknown = true)
data class RestResponsePage<T>(
  @JsonProperty("content") val content: List<T>,
  @JsonProperty("pageable") val pageable: Map<String, Any>,
  @JsonProperty("totalElements") val totalElements: Long,
  @JsonProperty("totalPages") val totalPages: Int,
  @JsonProperty("last") val last: Boolean,
  @JsonProperty("first") val first: Boolean,
  @JsonProperty("numberOfElements") val numberOfElements: Int,
  @JsonProperty("size") val size: Int,
  @JsonProperty("number") val number: Int,
  @JsonProperty("sort") val sort: Map<String, Any>,
  @JsonProperty("empty") val empty: Boolean,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PagedCaseListReferrals<T>(
  val pagedReferrals: RestResponsePage<T>,
  val otherTabTotal: Int,
  val filters: CaseListFilterValues,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PagedProgrammeDetails<T>(
  val group: Group,
  val filters: ProgrammeGroupDetails.Filters,
  val pagedGroupData: RestResponsePage<T>,
  val otherTabTotal: Int,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsByRegionResponse<T>(
  val pagedGroupData: RestResponsePage<T>,
  val otherTabTotal: Int,
  val regionName: String,
)
