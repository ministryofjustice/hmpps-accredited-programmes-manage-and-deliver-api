package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository

@Service
@Transactional
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
   *
   *  We have also seen that sometimes the `SOURCED_FROM` is incorrectly set in Interventions Manager, therefore we also
   *  need to try and fetch the opposite value to the current `SOURCED_FROM` if the call fails and update our referral value
   *  to match if the second call succeeds.
   */
  fun resolveIfEventNumberIsZero(referral: ReferralEntity): ReferralEntity? {
    if (referral.eventNumber != 0) return referral

    val eventId = referral.eventId ?: run {
      log.error("EventId for referral ${referral.id} is null.")
      return referral
    }

    log.info("Referral '${referral.id}' has event number 0. Attempting to resolve for CRN '${referral.crn}'.")

    val response = resolveSource(referral, eventId)

    if (response != null) {
      referral.eventNumber = response.eventNumber
      referralRepository.save(referral)
      logSuccess(referral, response.eventNumber)
    } else {
      logFailureEvent(referral)
    }

    return referral
  }

  private fun resolveSource(
    referral: ReferralEntity,
    eventId: String,
  ): NDeliusCaseRequirementOrLicenceConditionResponse? = when (referral.sourcedFrom) {
    ReferralEntitySourcedFrom.REQUIREMENT -> getRequirement(referral, eventId)

    ReferralEntitySourcedFrom.LICENCE_CONDITION -> getLicenceCondition(referral, eventId)

    else -> {
      log.error("${referral.sourcedFrom} is not a valid value")
      return null
    }
  }

  private fun getLicenceCondition(
    referral: ReferralEntity,
    eventId: String,
  ): NDeliusCaseRequirementOrLicenceConditionResponse? {
    log.info("...attempting to retrieve a Licence Condition for Referral with ID: ${referral.id}")
    return when (
      val response =
        nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, eventId)
    ) {
      is ClientResult.Success -> {
        telemetryClient.logToAppInsights(
          "${GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.eventName}.success",
          mapOf(
            "integrationActionType" to GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.name,
            "outcome" to "success",
          ),
        )

        response.body
      }

      else -> {
        log.error("Could not fetch a Licence condition with ID $eventId, for Referral with ID: ${referral.id}")
        telemetryClient.logToAppInsights(
          "${GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.name,
            "outcome" to "failure",
          ),
        )
        null
      }
    }
  }

  private fun getRequirement(
    referral: ReferralEntity,
    eventId: String,
  ): NDeliusCaseRequirementOrLicenceConditionResponse? {
    log.info("...attempting to retrieve a Requirement for Referral with ID: ${referral.id}")
    return when (
      val response =
        nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, eventId)
    ) {
      is ClientResult.Success -> {
        telemetryClient.logToAppInsights(
          "${IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.eventName}.success",
          mapOf(
            "integrationActionType" to IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.name,
            "outcome" to "success",
          ),
        )
        response.body
      }

      else -> {
        log.error("Could not fetch a Requirement with ID $eventId, for Referral with ID: ${referral.id}")
        telemetryClient.logToAppInsights(
          "${IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.name,
            "outcome" to "failure",
          ),
        )
        null
      }
    }
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
