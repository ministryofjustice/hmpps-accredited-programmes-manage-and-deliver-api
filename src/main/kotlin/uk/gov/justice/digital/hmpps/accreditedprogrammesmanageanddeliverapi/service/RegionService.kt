package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UserTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CodeDescription
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_PDU_OFFICE_LOCATION_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_REGION_PDU_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.TelemetryService

@Service
class RegionService(
  private val nDeliusApiIntegrationApiClient: NDeliusIntegrationApiClient,
  private val telemetryService: TelemetryService,
) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun getPdusForRegion(regionCode: String): List<CodeDescription> = when (val result = nDeliusApiIntegrationApiClient.getPdusForRegion(regionCode)) {
    is ClientResult.Success -> {
      val pduNames = result.body.pdus
      log.debug("Region code: {} returned pduNames: {}", regionCode, pduNames.map { it.description }.distinct())
      telemetryService.logToAppInsights(
        eventName = "${GET_REGION_PDU_N_DELIUS.eventName}.success",
        integrationActionType = GET_REGION_PDU_N_DELIUS.name,
        outcome = "success",
      )
      pduNames
        .map { CodeDescription(it.code, it.description) }
        .sortedBy { it.description.lowercase() }
        .ifEmpty {
          log.warn("No PDU's returned for regionCode: $regionCode")
          emptyList()
        }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch PDU's for regionCode: $regionCode:  ${result.toException().message}")
      telemetryService.logToAppInsights(
        eventName = "${GET_REGION_PDU_N_DELIUS.eventName}.failure",
        integrationActionType = GET_REGION_PDU_N_DELIUS.name,
        outcome = "failure",
      )

      emptyList()
    }
  }

  fun getOfficeLocationsForPdu(pduCode: String): List<CodeDescription> = when (val result = nDeliusApiIntegrationApiClient.getOfficeLocationsForPdu(pduCode)) {
    is ClientResult.Success -> {
      val officeNames = result.body.officeLocations
      log.debug("Pdu code: {} returned officeNames: {}", pduCode, officeNames.map { it.description }.distinct())
      telemetryService.logToAppInsights(
        eventName = "${GET_PDU_OFFICE_LOCATION_N_DELIUS.eventName}.success",
        integrationActionType = GET_PDU_OFFICE_LOCATION_N_DELIUS.name,
        outcome = "success",
      )
      officeNames
        .sortedBy { it.description.lowercase() }
        .ifEmpty {
          log.warn("No office location's returned for pduCode: $pduCode")
          telemetryService.logToAppInsights(
            eventName = "${GET_PDU_OFFICE_LOCATION_N_DELIUS.eventName}.failure",
            integrationActionType = GET_PDU_OFFICE_LOCATION_N_DELIUS.name,
            outcome = "failure",
          )

          emptyList()
        }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch office location's for pduCode: $pduCode:  ${result.toException().message}")
      emptyList()
    }
  }

  fun getTeamMembersForPdu(regionCode: String): List<UserTeamMember> = when (val result = nDeliusApiIntegrationApiClient.getPdusForRegion(regionCode)) {
    is ClientResult.Success -> {
      logTelemetry("success")
      val pdus = result.body.pdus
      if (pdus.isEmpty()) {
        log.warn("No pdus found in region: $regionCode")
      }
      pdus
        .flatMap { pdu ->
          pdu.team.flatMap { team ->
            team.members.map { member ->
              UserTeamMember(
                personCode = member.code,
                personName = member.name.getNameAsString(),
                teamName = team.description,
                teamCode = team.code,
              )
            }
          }
        }
        // Filter out any duplicates which are returned
        .distinctBy { it.personCode to it.personName }
    }

    is ClientResult.Failure -> {
      log.error("Failed to fetch team members for region: $regionCode ${result.toException().message}")
      logTelemetry("failure")
      emptyList()
    }
  }

  private fun logTelemetry(outcome: String) {
    telemetryService.logToAppInsights(
      eventName = "${GET_REGION_PDU_N_DELIUS.eventName}.$outcome",
      integrationActionType = GET_REGION_PDU_N_DELIUS.name,
      outcome = outcome,
    )
  }
}
