package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.FindAndReferReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
open class ReferralService(
  private val findAndReferInterventionApiClient: FindAndReferInterventionApiClient,
  private val ndeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val referralRepository: ReferralRepository,
  private val serviceUserService: ServiceUserService,
  private val cohortService: CohortService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralDetails(referralId: UUID): ReferralDetails? {
    val referral = referralRepository.findByIdOrNull(referralId) ?: return null
    val personalDetails = serviceUserService.getPersonalDetailsByIdentifier(referral.crn)
    return ReferralDetails.toModel(referral, personalDetails)
  }

  fun getFindAndReferReferralDetails(referralId: UUID): FindAndReferReferralDetails {
    val referralDetails =
      when (val result = findAndReferInterventionApiClient.getFindAndReferReferral(referralId = referralId)) {
        is ClientResult.Failure -> {
          log.warn("Failure to retrieve referral details for uuid : $referralId")
          throw NotFoundException("No referral details found for id: $referralId")
        }

        is ClientResult.Success -> result.body
      }
    return referralDetails
  }

  fun createReferral(findAndReferReferralDetails: FindAndReferReferralDetails) {
    val cohort = cohortService.determineOffenceCohort(findAndReferReferralDetails.personReference)

    val statusHistoryEntity = ReferralStatusHistoryEntity(
      status = "Created",
      startDate = LocalDateTime.now(),
      endDate = null,
    )

    val referralEntity = findAndReferReferralDetails.toReferralEntity(mutableListOf(statusHistoryEntity), cohort)

    log.info("Inserting referral for Intervention: '${referralEntity.interventionName}' and Crn: '${referralEntity.crn}' with cohort: $cohort")
    referralRepository.save(referralEntity)
  }

  fun getReferralById(id: UUID): ReferralEntity? = referralRepository.findByIdOrNull(id)

  fun attemptToFindManagerForReferral(referralId: UUID): RequirementOrLicenceConditionManager? {
    val referral = getReferralById(referralId)

    if (referral == null) {
      throw NotFoundException("No Referral found for id: $referralId")
    } else if (referral.eventId.isNullOrEmpty()) {
      throw NotFoundException("Referral with id: $referralId exists, but has no eventId")
    }

    val referralIdString = referral.id.toString()
    val eventId: String = referral.eventId!!

    log.info("Attempting to retrieve a Requirement for Referral with ID: $referralId...")
    // TODO: Check to see if the sourced_from field is set, then make an informed opinion based on that --TJWC 2025-09-04

    when (val response = ndeliusIntegrationApiClient.getRequirementManagerDetails(referralIdString, eventId)) {
      is ClientResult.Success -> {
        log.info("...success! Found a Requirement for referral with ID: $referralId")
        return response.body.manager
      }
      is ClientResult.Failure.StatusCode -> {
        if (response.status.value() == 404) {
          log.info("...could not find Requirement for Referral with ID: $referralId")
        }
      }
      else -> {
        log.error("...failure, encountered an error while fetching Requirement for Referral with ID: $referralId from nDelius Integration API")
        throw NotFoundException("Could not fetch a Requirement with ID $eventId, for Referral with ID: $referralId")
      }
    }

    log.info("...attempting to retrieve a Licence Condition for Referral with ID: $referralId")

    when (val response = ndeliusIntegrationApiClient.getLicenceConditionManagerDetails(referralIdString, eventId)) {
      is ClientResult.Success -> {
        log.info("...success! Found a Licence Condition for referral with ID: $referralId")
        return response.body.manager
      }
      else -> {
        log.error("...failure, neither a Requirement or Licence Condition returned for Referral with ID $referralId")
        return null
      }
    }

    return null
  }
}
