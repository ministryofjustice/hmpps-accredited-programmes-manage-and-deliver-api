package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers

@Service
class DeliveryLocationService(private val nDeliusApiIntegrationApiClient: NDeliusIntegrationApiClient) {

  val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun getPdusForRegion(regionCode: String): List<NDeliusRegionWithMembers.NDeliusPduWithTeam> = when (val result = nDeliusApiIntegrationApiClient.getPdusForRegion(regionCode)) {
    is ClientResult.Success -> {
      val pduNames = result.body.pdus
      pduNames.ifEmpty {
        log.warn("No PDU's returned for regionCode: $regionCode")
        emptyList()
      }
    }

    is ClientResult.Failure -> {
      emptyList()
    }
  }
}
