package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

private const val FIRST_VALID_EVENT_NUMBER = 1
private const val LAST_VALID_EVENT_NUMBER = 20

@Service
class ReferralEventNumberResolverService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val referralRepository: ReferralRepository,
  private val telemetryClient: TelemetryClient,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * When Referrals are populated by the data-importer-service, they have the eventNumber of 0, we know that this
   * is an invalid number, and so when we attempt to fetch offence details associated with it, we will always get
   * a 4xx error.  It does not appear that the eventNumber is stored in Interventions Manager data, and therefore
   * we need a naieve solution which simply:
   * 1. Recognises that an event number is 0
   * 2. Iterates up, one at a time (until 20), to see if we can get a 2xx response from nDelius
   * 3. If no success is found, we leave the event number as 0
   */
  @Transactional
  fun resolveIfEventNumberIsZero(referral: ReferralEntity): Int? {
    if (referral.eventNumber != 0) {
      return referral.eventNumber
    }

    log.info(
      "Referral {} has event number 0. Attempting to resolve by checking nDelius sentence endpoint for CRN {} ({}..{}).",
      referral.id,
      referral.crn,
      FIRST_VALID_EVENT_NUMBER,
      LAST_VALID_EVENT_NUMBER,
    )

    for (candidateEventNumber in FIRST_VALID_EVENT_NUMBER..LAST_VALID_EVENT_NUMBER) {
      when (val response = nDeliusIntegrationApiClient.getSentenceInformation(referral.crn, candidateEventNumber)) {
        is ClientResult.Success -> {
          referral.eventNumber = candidateEventNumber
          referralRepository.save(referral)
          logSuccess(referral, candidateEventNumber)
          return candidateEventNumber
        }

        is ClientResult.Failure.StatusCode -> {
          log.info(
            "Event number {} was not valid for Referral with ID '{}' (status {}). Trying next.",
            candidateEventNumber,
            referral.id,
            response.status.value(),
          )
        }

        is ClientResult.Failure.Other -> {
          log.warn(
            "Could not validate event number {} for Referral with ID '{}' due to: {}. Trying next.",
            candidateEventNumber,
            referral.id,
            response.getErrorMessage(),
          )
        }
      }
    }

    logFailureEvent(referral)
    return referral.eventNumber
  }

  private fun logFailureEvent(referral: ReferralEntity) {
    log.warn(
      "Could not resolve a valid event number for Referral with ID '{}' after checking {} to {}. Keeping event number as 0.",
      referral.id,
      FIRST_VALID_EVENT_NUMBER,
      LAST_VALID_EVENT_NUMBER,
    )

    telemetryClient.logToAppInsights(
      "Referral.event-number-resolution.failure",
      mapOf(
        "referralId" to referral.id.toString(),
      ),
    )
  }

  private fun logSuccess(referral: ReferralEntity, newEventNumber: Int) {
    log.info(
      "Resolved event number for Referral with ID '{}' - New event number is '{}'.",
      referral.id,
      newEventNumber,
    )

    telemetryClient.logToAppInsights(
      "Referral.event-number-resolution.success",
      mapOf(
        "referralId" to referral.id.toString(),
        "newEventNumber" to newEventNumber.toString(),
      ),
    )
  }
}
