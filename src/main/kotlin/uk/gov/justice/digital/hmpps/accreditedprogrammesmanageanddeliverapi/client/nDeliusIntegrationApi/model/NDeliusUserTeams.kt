package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Response from GET /user/{username}/teams endpoint
 * As far as we're aware, this can be 0..n in size
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusUserTeams(
  val teams: List<NDeliusUserTeam>,
)

/**
 * Represents a single nDelius team that a user belongs to
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class NDeliusUserTeam(
  val code: String,
  val description: String,
  val pdu: CodeDescription,
  val region: CodeDescription,
)
