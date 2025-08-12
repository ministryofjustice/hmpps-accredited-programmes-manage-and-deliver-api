package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse

@Service
class SentenceService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
) {

  fun getSentenceInformationByIdentifier(crn: String, eventNumber: Int?): NDeliusSentenceResponse = when (val result = nDeliusIntegrationApiClient.getSentenceInformation(crn, eventNumber)) {
    is ClientResult.Success -> result.body
    is ClientResult.Failure -> result.throwException()
  }
}
