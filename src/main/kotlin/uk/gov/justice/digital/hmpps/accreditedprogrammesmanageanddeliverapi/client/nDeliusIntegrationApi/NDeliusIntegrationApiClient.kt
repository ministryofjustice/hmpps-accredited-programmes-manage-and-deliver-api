package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
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

  /**
   * For a Referral which was created from a Requirement (not a Licence Condition) fetch details of the staff
   * member associated with that Requirement.
   * @see getLicenceConditionManagerDetails for Referrals created via Licence Condition
   *
   * @param crn - CRN for the Person on Probation
   * @param requirementId - Unique identifier for the Requirement which triggered the creation of the Referral
   */
  fun getRequirementManagerDetails(crn: String, requirementId: String) = getRequest<NDeliusCaseRequirementOrLicenceConditionResponse>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/requirement/$requirementId"
  }

  /**
   * For a Referral which was created from a Licence Condition (not a Referral) fetch details of the staff
   * member associated with that Licence Condition.
   * @see getRequirementManagerDetails for Referrals created via Requirement
   *
   * @param crn - CRN for the Person on Probation
   * @param licenceConditionId - Unique identifier for the Licence Condition which triggered the creation of the Referral
   */
  fun getLicenceConditionManagerDetails(crn: String, licenceConditionId: String) = getRequest<NDeliusCaseRequirementOrLicenceConditionResponse>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/licence-conditions/$licenceConditionId"
  }
}
