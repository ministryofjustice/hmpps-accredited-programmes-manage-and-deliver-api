package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.DeleteAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LicenceConditions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LimitedAccessOffenderCheckResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegionWithMembers
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusRegistrations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusSentenceResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusUserTeams
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Offences
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RegionDto
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Requirements
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ServiceUnavailableException

private const val N_DELIUS_INTEGRATION_API = "NDelius Integration API"

@Component
class NDeliusIntegrationApiClient(
  @Qualifier("nDeliusIntegrationWebClient") webClient: WebClient,
  objectMapper: ObjectMapper,
) : BaseHMPPSClient(webClient, objectMapper) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getPersonalDetails(identifier: String) = getRequest<NDeliusPersonalDetails>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$identifier/personal-details"
  }

  fun getSentenceInformation(crn: String, eventNumber: Int?) = getRequest<NDeliusSentenceResponse>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/sentence/$eventNumber"
  }

  fun verifyLimitedAccessOffenderCheck(
    username: String,
    identifiers: List<String>,
  ): ClientResult<LimitedAccessOffenderCheckResponse> {
    require(identifiers.size <= 500) { "Identifier limit exceeded: ${identifiers.size} identifiers provided, maximum is 500" }

    return postRequest<LimitedAccessOffenderCheckResponse>(
      N_DELIUS_INTEGRATION_API,
    ) {
      path = "/user/$username/access"
      body = identifiers
    }
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

  fun getLicenceConditions(crn: String) = getRequest<LicenceConditions>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/licence-conditions"
  }

  fun getRequirements(crn: String) = getRequest<Requirements>(N_DELIUS_INTEGRATION_API) {
    path = "/case/$crn/requirements"
  }

  /**
   * Fetch the teams associated with a given user from nDelius.
   * This is used to determine which regions a user has access to.
   *
   * Retries on transient failures (connection-level errors / timeouts via
   * [ClientResult.Failure.Other], and 5xx via [ServiceUnavailableException]) — confirmed via
   * production telemetry (2026-10-08) that this specific NDelius call fails intermittently and
   * the identical lookup succeeds again within seconds for the same account. Does **not** retry
   * [ClientResult.Failure.StatusCode] (e.g. a genuine 4xx) — that's a real negative result, not a
   * transient failure, and retrying it would be wasteful/could mask an actual problem.
   *
   * See `doc/planning/manage-and-deliver-region-404/agent-handover-fix-region-404.md` in the
   * sibling `hmpps-accredited-programmes-api` repo for the full investigation this fix is based
   * on.
   *
   * @param username - The hmpps-auth username of the user
   * @return List of teams with their associated regions
   */
  fun getTeamsForUser(username: String): ClientResult<NDeliusUserTeams> = withRetryOnTransientFailure(
    operationDescription = "getTeamsForUser(username=$username)",
  ) {
    getRequest<NDeliusUserTeams>(N_DELIUS_INTEGRATION_API) {
      path = "/user/$username/teams"
    }
  }

  /**
   * Retries [block] up to [maxAttempts] times (with exponential backoff starting at
   * [initialDelayMs]) on transient failures only:
   * - a thrown [ServiceUnavailableException] (5xx from the downstream service — see
   *   `BaseHMPPSClient.doRequest`), or
   * - a returned [ClientResult.Failure.Other] (connection-level/timeout exceptions caught by
   *   `BaseHMPPSClient`'s generic `catch (exception: Exception)` branch).
   *
   * Does NOT retry [ClientResult.Failure.StatusCode] (a genuine non-2xx HTTP response, e.g. 4xx) —
   * that's a real result, not a transient failure. Logs a WARN on every retry attempt so this is
   * visible in telemetry going forward (previously, retries/failures here were only visible as an
   * ambiguous downstream 404 with no record of the intermediate failure — see the handover doc's
   * note on weak existing telemetry).
   *
   * Worst-case latency: this call runs synchronously (blocking `.block()` inside
   * `BaseHMPPSClient`), and the shared `nDeliusIntegrationWebClient` is built via
   * `WebClientConfiguration.buildWebClient`, which uses `@Value("\${api.timeout:60s}")` (no
   * override configured for this service, so the 60s default applies). So a true worst case of 3
   * full timeouts plus backoff is ~181.5s of a blocked request thread. Accepted as a deliberate
   * trade-off here: this is a single, low-traffic, non-hot-path endpoint (`/current-user/region`,
   * called once per user session and then cached — see `UserService.getUserRegions`'s
   * `@Cacheable`), and evidence from the 2026-10-08 production investigation points to fast
   * connection-level failures (`ClientResult.Failure.Other` wrapping a connection-reset exception)
   * rather than full 60s timeouts actually occurring in practice. If this call is ever used from a
   * hot path, or if full-timeout failures start appearing in telemetry, revisit the attempt
   * count/backoff or add a shorter per-call timeout instead of reusing the shared 60s default.
   */
  private fun <T> withRetryOnTransientFailure(
    operationDescription: String,
    maxAttempts: Int = 3,
    initialDelayMs: Long = 500,
    block: () -> ClientResult<T>,
  ): ClientResult<T> {
    var delayMs = initialDelayMs
    for (attempt in 1..maxAttempts) {
      val result = try {
        block()
      } catch (exception: ServiceUnavailableException) {
        if (attempt >= maxAttempts) throw exception
        log.warn(
          "Transient failure on $operationDescription (attempt $attempt/$maxAttempts): " +
            "${exception.message}. Retrying in ${delayMs}ms.",
        )
        Thread.sleep(delayMs)
        delayMs *= 2
        continue
      }

      if (result is ClientResult.Failure.Other && attempt < maxAttempts) {
        log.warn(
          "Transient failure on $operationDescription (attempt $attempt/$maxAttempts): " +
            "${result.getErrorMessage()}. Retrying in ${delayMs}ms.",
        )
        Thread.sleep(delayMs)
        delayMs *= 2
        continue
      }

      return result
    }
    error("unreachable: loop always returns or throws by the final attempt")
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
   * Fetch accredited programmes members for the given region code.
   *
   * @param regionCode - The code of the region in NDelius.
   * @return Region with a list of teams and their members
   */
  fun getAccreditedProgrammesMembersByRegionCode(regionCode: String) = getRequest<RegionDto>(N_DELIUS_INTEGRATION_API) {
    path = "/regions/$regionCode/local-admin-units/accredited-programmes/members"
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

  fun createAppointmentsInDelius(appointments: CreateAppointmentRequest) = postRequest<Unit>(N_DELIUS_INTEGRATION_API) {
    path = "/appointments"
    body = appointments
  }

  fun deleteAppointmentsInDelius(appointments: DeleteAppointmentsRequest) = deleteRequest<Unit>(N_DELIUS_INTEGRATION_API) {
    path = "/appointments"
    body = appointments
  }

  fun updateAppointmentsInDelius(appointments: UpdateAppointmentsRequest) = putRequest<Unit>(N_DELIUS_INTEGRATION_API) {
    path = "/appointments"
    body = appointments
  }
}
