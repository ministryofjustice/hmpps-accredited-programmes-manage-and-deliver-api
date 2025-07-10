package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheck
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.OffenderIdentifiers

@Component
class NDeliusIntegrationApiClient(
  @Qualifier("nDeliusIntegrationWebClient") webClient: WebClient,
  objectMapper: ObjectMapper
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getOffenderIdentifiers(identifier: String) = getRequest<OffenderIdentifiers>("NDelius Integration API") {
      path = "/person/find/$identifier"
    }

  fun verifyLaoc(username: String, identifier: String) = getRequest<LimitedAccessOffenderCheck>("NDelius Integration API") {
    path = "/users/$username/access/$identifier"
  }
}
