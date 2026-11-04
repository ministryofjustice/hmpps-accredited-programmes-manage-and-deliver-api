package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegionDto(
  val teams: List<TeamDto> = emptyList(),
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class TeamDto(
    val code: String,
    val description: String,
    val members: List<MemberDto>,
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class MemberDto(
    val code: String,
    val name: FullName,
  )
}
