package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusRegionWithMembers(
  val code: String,
  val description: String,
  val pdus: List<NDeliusPduWithTeam>,
) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  data class NDeliusPduWithTeam(
    val code: String,
    val description: String,
    val team: List<NDeliusUserTeamWithMembers>,
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class NDeliusUserTeamWithMembers(
    val code: String,
    val description: String,
    val members: List<NDeliusUserTeamMembers>,
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class NDeliusUserTeamMembers(
    val code: String,
    val name: FullName,
  )
}
