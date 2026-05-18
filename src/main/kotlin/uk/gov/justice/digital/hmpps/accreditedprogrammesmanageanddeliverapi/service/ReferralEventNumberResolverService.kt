package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

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
   * we need to fetch this value from one of two delius endpoints
   *  - /case/${crn}/licence-conditions/{id}
   *  - /case/${crn}/requirement/{id}
   */
  @Transactional
  fun resolveIfEventNumberIsZero(referral: ReferralEntity): Int? {
    if (referral.eventNumber != 0) {
      return referral.eventNumber
    }

    log.info(
      "Referral with ID '${referral.id}' has event number 0. Attempting to resolve by checking nDelius sentence endpoint for CRN '${referral.crn}'.",
    )

    val eventNumberResponse = when (referral.sourcedFrom) {
      ReferralEntitySourcedFrom.REQUIREMENT -> {
        log.info("...attempting to retrieve a Requirement for Referral with ID: ${referral.id}")
        when (
          val response =
            nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, referral.eventId!!)
        ) {
          is ClientResult.Success -> response.body

          else -> {
            log.error("Could not fetch a Requirement with ID ${referral.eventId}, for Referral with ID: ${referral.id}")
            null
          }
        }
      }

      ReferralEntitySourcedFrom.LICENCE_CONDITION -> {
        log.info("...attempting to retrieve a Licence Condition for Referral with ID: ${referral.id}")
        when (
          val response =
            nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, referral.eventId!!)
        ) {
          is ClientResult.Success -> response.body

          else -> {
            log.error("Could not fetch a Licence condition with ID ${referral.eventId}, for Referral with ID: ${referral.id}")
            null
          }
        }
      }

      else -> null
    }

    if (eventNumberResponse != null) {
      referral.eventNumber = eventNumberResponse.eventNumber
      referralRepository.save(referral)
      logSuccess(referral, eventNumberResponse.eventNumber)
    } else {
      logFailureEvent(referral)
    }

    return referral.eventNumber
  }

  private fun logFailureEvent(referral: ReferralEntity) {
    log.warn(
      "Could not resolve a valid event number for Referral with ID '${referral.id}'. Keeping event number as 0.",
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
      "Resolved event number for Referral with ID '${referral.id}' - New event number is '$newEventNumber'.",
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
