package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeam
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams

class NDeliusUserTeamsFactory {
  private var teams: List<NDeliusUserTeam> = listOf(
    NDeliusUserTeam(
      code = "TEAM001",
      description = "Test Team 1",
      pdu = CodeDescription("PDU001", "Test PDU 1"),
      region = CodeDescription("REGION001", "Test Region 1"),
    ),
  )

  fun withTeams(teams: List<NDeliusUserTeam>) = apply { this.teams = teams }

  fun withSingleTeam(
    code: String? = "TEAM001",
    description: String? = "Test Team",
    pduCode: String? = "PDU001",
    pduDescription: String? = "Test PDU",
    regionCode: String? = "REGION001",
    regionDescription: String? = "Test Region",
  ) = apply {
    this.teams = listOf(
      NDeliusUserTeam(
        code = code!!,
        description = description!!,
        pdu = CodeDescription(pduCode!!, pduDescription!!),
        region = CodeDescription(regionCode!!, regionDescription!!),
      ),
    )
  }

  fun produce() = NDeliusUserTeams(teams = this.teams)
}
