package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.LicenceConditions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.Requirements
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_LICENCE_CONDITIONS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_REQUIREMENTS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.TelemetryUtils

private const val REQUIREMENT_BUILDING_CHOICES_SUBCATEGORY_CODE = "734"
private const val LICENCE_CONDITION_BUILDING_CHOICES_SUBCATEGORY_CODE = "LC266"
private const val BUILDING_CHOICES_SUBCATEGORY_DESCRIPTION = "Building Choices"

@Service
@Transactional
class ReferralEventNumberResolverService(
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val referralRepository: ReferralRepository,
  private val telemetryUtils: TelemetryUtils,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
    const val INVALID_EVENT_NUMBER = 0
  }

  fun resolveAllEventNumbers() {
    log.info("Received request to resolve referral event numbers")
    val referralsWithInvalidEventNumbers = referralRepository.findByEventNumber(INVALID_EVENT_NUMBER)
    if (referralsWithInvalidEventNumbers.isEmpty()) {
      log.info("Nothing to do - No referrals with invalid event numbers found")
      return
    }
    log.info("Found ${referralsWithInvalidEventNumbers.size} referrals with invalid event numbers")

    var referralsUpdated = 0
    referralsWithInvalidEventNumbers.forEach { referral ->
      if (resolveEventNumber(referral)) {
        referralsUpdated++
      }
    }
    log.info("Successfully resolved event numbers for $referralsUpdated out of ${referralsWithInvalidEventNumbers.size} referrals")
  }

  private fun resolveEventNumber(referral: ReferralEntity): Boolean {
    if (referral.eventNumber != INVALID_EVENT_NUMBER) return false

    return when (referral.sourcedFrom) {
      ReferralEntitySourcedFrom.REQUIREMENT -> resolveRequirement(referral)

      ReferralEntitySourcedFrom.LICENCE_CONDITION -> resolveLicenceCondition(referral)

      else -> {
        log.error("${referral.sourcedFrom} is not a valid value")
        false
      }
    }
  }

  private fun resolveLicenceCondition(referral: ReferralEntity): Boolean {
    val licenceConditions = getLicenceConditions(referral)

    licenceConditions?.content?.takeIf { it.isNotEmpty() }?.firstOrNull {
      it.subCategory?.code == LICENCE_CONDITION_BUILDING_CHOICES_SUBCATEGORY_CODE &&
        it.subCategory.description == BUILDING_CHOICES_SUBCATEGORY_DESCRIPTION
    }?.let {
      if (duplicateReferralDetailsExists(referral, it.id.toString())) {
        log.warn("Existing referral details found for crn: ${referral.crn}, event_id: ${it.id}, sourced_from: ${referral.sourcedFrom}")
        return false
      }
      referral.eventNumber = it.eventNumber.toInt()
      referral.eventId = it.id.toString()
      referralRepository.save(referral)
      logSuccess(referral, referral.eventNumber!!, referral.eventId!!)
      return true
    } ?: logFailureEvent(referral)
    return false
  }

  private fun resolveRequirement(referral: ReferralEntity): Boolean {
    val requirements = getRequirements(referral)

    val buildingChoicesRequirement = requirements?.content?.takeIf { it.isNotEmpty() }?.firstOrNull {
      it.subCategory?.code == REQUIREMENT_BUILDING_CHOICES_SUBCATEGORY_CODE &&
        it.subCategory.description == BUILDING_CHOICES_SUBCATEGORY_DESCRIPTION
    }
    buildingChoicesRequirement?.let {
      if (duplicateReferralDetailsExists(referral, it.id.toString())) {
        log.warn("Existing referral details found for crn: ${referral.crn}, event_id: ${it.id}, sourced_from: ${referral.sourcedFrom}")
        return false
      }
      referral.eventNumber = it.eventNumber.toInt()
      referral.eventId = it.id.toString()
      referralRepository.save(referral)
      logSuccess(referral, referral.eventNumber!!, referral.eventId!!)
      return true
    } ?: logFailureEvent(referral)
    return false
  }

  // A unique key on the referral table exists to prevent duplicate referrals being created for a given crn, event_id and sourced_from
  private fun duplicateReferralDetailsExists(
    referral: ReferralEntity,
    newEventId: String,
  ): Boolean = referralRepository.existsByCrnAndEventIdAndSourcedFrom(
    referral.crn,
    newEventId,
    referral.sourcedFrom!!,
  )

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
    if (referral.eventNumber != INVALID_EVENT_NUMBER) return referral

    val eventId = referral.eventId ?: run {
      log.error("EventId for referral ${referral.id} is null.")
      return referral
    }

    log.info("Referral '${referral.id}' has event number 0. Attempting to resolve for CRN '${referral.crn}'.")

    val response = resolveSource(referral, eventId)

    if (response != null) {
      referral.eventNumber = response.eventNumber
      referralRepository.save(referral)
      logSuccess(referral, response.eventNumber, referral.eventId.toString())
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

  private fun getLicenceConditions(referral: ReferralEntity): LicenceConditions? {
    log.info("Attempting to retrieve Licence Conditions for Referral with ID: ${referral.id}")
    return when (
      val response =
        nDeliusIntegrationApiClient.getLicenceConditions(referral.crn)
    ) {
      is ClientResult.Success -> {
        telemetryUtils.logToAppInsights(
          eventName = "${GET_LICENCE_CONDITIONS_N_DELIUS.eventName}.success",
          integrationActionType = GET_LICENCE_CONDITIONS_N_DELIUS.name,
          outcome = "success",
        )
        response.body
      }

      else -> {
        log.error("Could not fetch Licence conditions for referral with CRN ${referral.crn}")
        telemetryUtils.logToAppInsights(
          eventName = "${GET_LICENCE_CONDITIONS_N_DELIUS.eventName}.failure",
          integrationActionType = GET_LICENCE_CONDITIONS_N_DELIUS.name,
          outcome = "failure",
        )
        null
      }
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
        telemetryUtils.logToAppInsights(
          eventName = "${GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.eventName}.success",
          integrationActionType = GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.name,
          outcome = "success",
        )

        response.body
      }

      else -> {
        log.error("Could not fetch a Licence condition with ID $eventId, for Referral with ID: ${referral.id}")
        telemetryUtils.logToAppInsights(
          eventName = "${GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.eventName}.failure",
          integrationActionType = GET_LICENCE_CONDITION_MANAGER_DETAILS_N_DELIUS.name,
          outcome = "failure",
        )
        null
      }
    }
  }

  private fun getRequirements(referral: ReferralEntity): Requirements? {
    log.info("Attempting to retrieve requirements for Referral with ID: ${referral.id}")
    return when (
      val response =
        nDeliusIntegrationApiClient.getRequirements(referral.crn)
    ) {
      is ClientResult.Success -> {
        telemetryUtils.logToAppInsights(
          eventName = "${GET_REQUIREMENTS_N_DELIUS.eventName}.success",
          integrationActionType = GET_REQUIREMENTS_N_DELIUS.name,
          outcome = "success",
        )
        response.body
      }

      else -> {
        log.error("Could not fetch requirements for referral with CRN ${referral.crn}")
        telemetryUtils.logToAppInsights(
          eventName = "${GET_REQUIREMENTS_N_DELIUS.eventName}.failure",
          integrationActionType = GET_REQUIREMENTS_N_DELIUS.name,
          outcome = "failure",
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
        telemetryUtils.logToAppInsights(
          eventName = "${GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.eventName}.success",
          integrationActionType = GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.name,
          outcome = "success",
        )
        response.body
      }

      else -> {
        log.error("Could not fetch a Requirement with ID $eventId, for Referral with ID: ${referral.id}")
        telemetryUtils.logToAppInsights(
          eventName = "${GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.eventName}.failure",
          integrationActionType = GET_REQUIREMENT_MANAGER_DETAILS_N_DELIUS.name,
          outcome = "failure",
        )
        null
      }
    }
  }

  private fun logFailureEvent(referral: ReferralEntity) {
    log.warn(
      "Could not resolve a valid event number and event id for Referral with ID '${referral.id}'. Keeping event number as 0.",
    )

    telemetryUtils.logToAppInsights(
      eventName = "Referral.event-number-resolution.failure",
      properties = mapOf(
        "referralId" to referral.id.toString(),
      ),
    )
  }

  private fun logSuccess(referral: ReferralEntity, newEventNumber: Int, newEventId: String) {
    log.info(
      "Resolved event number for Referral with ID '${referral.id}' - New event number is '$newEventNumber'. New event ID is '$newEventId'.",
    )

    telemetryUtils.logToAppInsights(
      eventName = "Referral.event-number-resolution.success",
      properties = mapOf(
        "referralId" to referral.id.toString(),
        "newEventNumber" to newEventNumber.toString(),
        "newEventId" to newEventId,
      ),
    )
  }
}
