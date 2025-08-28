package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRequirementResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences

private const val N_DELIUS_INTEGRATION_API = "NDelius Integration API"

@Component
class NDeliusIntegrationApiClient(
  @Qualifier("nDeliusIntegrationWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  fun getPersonalDetails(identifier: String) = getRequest<NDeliusPersonalDetails>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$identifier/personal-details"
  }

  fun getSentenceInformation(crn: String, eventNumber: Int?) = getRequest<NDeliusSentenceResponse>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/sentence/$eventNumber"
  }

  fun verifyLimitedAccessOffenderCheck(username: String, identifiers: List<String>) = postRequest<LimitedAccessOffenderCheckResponse>(
    N_DELIUS_INTEGRATION_API,
  ) {
    path = "/user/$username/access"
    body = identifiers
  }

  fun getOffences(crn: String, eventNumber: Int) = getRequest<Offences>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/sentence/$eventNumber/offences"
  }

  fun getRegistrations(crn: String) = getRequest<NDeliusRegistrations>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/registrations"
  }

  //  TODO: confirm what a `requirementId` is in relation to this model APG-1222 TJWC 2025-08-27
  fun getRequirementManagerDetails(crn: String, requirementId: String) = getRequest<NDeliusRequirementResponse>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/requirement/$requirementId"
  }
}
