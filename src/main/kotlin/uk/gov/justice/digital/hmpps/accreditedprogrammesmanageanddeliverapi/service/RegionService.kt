package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString

@Service
class RegionService(private val nDeliusApiIntegrationApiClient: NDeliusIntegrationApiClient) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun getPdusForRegion(regionCode: String): List<NDeliusRegionWithMembers.NDeliusPduWithTeam> = when (val result = nDeliusApiIntegrationApiClient.getPdusForRegion(regionCode)) {
    is ClientResult.Success -> {
      val pduNames = result.body.pdus
      log.debug("Region code: {} returned pduNames: {}", regionCode, pduNames)
      pduNames.ifEmpty {
        log.warn("No PDU's returned for regionCode: $regionCode")
        emptyList()
      }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch PDU's for regionCode: $regionCode:  ${result.toException().message}")
      emptyList()
    }
  }

  fun getOfficeLocationsForPdu(pduCode: String): List<CodeDescription> = when (val result = nDeliusApiIntegrationApiClient.getOfficeLocationsForPdu(pduCode)) {
    is ClientResult.Success -> {
      val officeNames = result.body.officeLocations
      log.debug("Pdu code: {} returned officeNames: {}", pduCode, officeNames)
      officeNames.ifEmpty {
        log.warn("No office location's returned for pduCode: $pduCode")
        emptyList()
      }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch office location's for pduCode: $pduCode:  ${result.toException().message}")
      emptyList()
    }
  }

  fun getTeamMembersForPdu(regionCode: String, pduCode: String): List<UserTeamMember> {
    when (val result = nDeliusApiIntegrationApiClient.getPdusForRegion(regionCode)) {
      is ClientResult.Success -> {
        val pdu: NDeliusRegionWithMembers.NDeliusPduWithTeam? = result.body.pdus.find { it.code == pduCode }
        if (pdu == null) {
          log.warn("No pdu found in region: $regionCode for pduCode: $pduCode")
          return emptyList()
        }
        return pdu.team.flatMap {
          it.members.map { member ->
            UserTeamMember(
              member.code,
              member.name.getNameAsString(),
            )
          }
        }
      }

      is ClientResult.Failure -> {
        log.error("Failed to fetch team members for region: $regionCode and pdu: $pduCode:  ${result.toException().message}")
        return emptyList()
      }
    }
  }
}
