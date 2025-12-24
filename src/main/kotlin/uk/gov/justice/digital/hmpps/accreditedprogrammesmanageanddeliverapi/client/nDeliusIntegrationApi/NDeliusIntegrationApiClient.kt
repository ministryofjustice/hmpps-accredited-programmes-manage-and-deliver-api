package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
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

  /**
   * Fetch the teams associated with a given user from nDelius.
   * This is used to determine which regions a user has access to.
   *
   * @param username - The hmpps-auth username of the user
   * @return List of teams with their associated regions
   */
  fun getTeamsForUser(username: String) = getRequest<NDeliusUserTeams>(N_DELIUS_INTEGRATION_API) {
    path = "/user/$username/teams"
  }

  /**
   * Fetch the pdus, teams and members of that team in a region.
   *
   * @param regionCode - The code of the region in NDelius.
   * @return List of pdus with their associated teams and members
   */
  fun getPdusForRegion(regionCode: String) = getRequest<NDeliusRegionWithMembers>(N_DELIUS_INTEGRATION_API) {
    path = "/regions/$regionCode/members"
  }

  /**
   * Fetch the Office locations for a PDU from NDelius.
   *
   * @param pduCode - The code of the PDU in NDelius.
   * @return List of office locations for a PDU.
   */
  fun getOfficeLocationsForPdu(pduCode: String) = getRequest<NDeliusApiProbationDeliveryUnitWithOfficeLocations>(N_DELIUS_INTEGRATION_API) {
    path = "/regions/pdu/$pduCode/office-locations"
  }

  /**
   * Create an appointment in NDelius.
   *
   */
  fun createAppointmentsInDelius(appointments: CreateAppointmentRequest) = postRequest<Void>(N_DELIUS_INTEGRATION_API) {
    path = "/appointments"
  }
}
