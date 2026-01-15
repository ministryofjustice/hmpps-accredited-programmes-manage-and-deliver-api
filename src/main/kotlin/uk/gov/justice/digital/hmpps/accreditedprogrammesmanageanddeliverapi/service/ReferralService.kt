package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.Referral
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.StatusUpdateResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ldc.UpdateLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.FindAndReferInterventionApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.FindAndReferReferralDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.findAndReferInterventionApi.model.toReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusApiProbationDeliveryUnitWithOfficeLocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusCaseRequirementOrLicenceConditionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.NDeliusPersonalDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.RequirementOrLicenceConditionManager
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.getNameAsString
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.PniResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.hasLdc
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.oasysApi.model.toPniScore
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralLdcHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralReportingLocationEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralLdcHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class ReferralService(
  private val findAndReferInterventionApiClient: FindAndReferInterventionApiClient,
  private val ndeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val referralRepository: ReferralRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository,
  private val userService: UserService,
  private val cohortService: CohortService,
  private val pniService: PniService,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val referralLdcHistoryRepository: ReferralLdcHistoryRepository,
  private val ldcService: LdcService,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val sentenceService: SentenceService,
  private val programmeGroupMembershipService: ProgrammeGroupMembershipService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  suspend fun refreshPersonalDetailsForReferral(referralId: UUID): ReferralDetails? = coroutineScope {
    val referral = referralRepository.findByIdWithMemberships(referralId) ?: return@coroutineScope null

    val hasLdcDeferred = async(Dispatchers.IO) {
      pniService.getPniCalculation(referral.crn).hasLdc()
    }

    val personalDetailsDeferred = async(Dispatchers.IO) {
      userService.getPersonalDetailsByIdentifier(referral.crn)
    }

    val sentenceEndDateDeferred = async(Dispatchers.IO) {
      sentenceService.getSentenceEndDate(
        referral.crn,
        referral.eventNumber,
        referral.sourcedFrom,
      )
    }

    val hasLdc = hasLdcDeferred.await()

    if (!ldcService.hasOverriddenLdcStatus(referralId)) {
      ldcService.updateLdcStatusForReferral(referral, UpdateLdc(hasLdc))
    }

    val personalDetails = personalDetailsDeferred.await()
    val sentenceEndDate = sentenceEndDateDeferred.await()

    updateReferralDetails(referral, personalDetails, sentenceEndDate)

    val referralLdc = referralLdcHistoryRepository.findTopByReferralIdOrderByCreatedAtDesc(referralId)?.hasLdc
    val latestReferralStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
    val allocatedGroup = programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral)
    ReferralDetails.toModel(referral, personalDetails, referralLdc, latestReferralStatus!!, allocatedGroup)
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

  fun createReferral(findAndReferReferralDetails: FindAndReferReferralDetails): ReferralEntity {
    val pniCalculation = getPni(findAndReferReferralDetails)
    val sentenceEndDate = sentenceService.getSentenceEndDate(
      findAndReferReferralDetails.personReference,
      findAndReferReferralDetails.eventNumber,
      findAndReferReferralDetails.sourcedFromReferenceType,
    )

    val cohort =
      pniCalculation?.let { cohortService.determineOffenceCohort(it.toPniScore()) } ?: OffenceCohort.GENERAL_OFFENCE
    val hasLdc = pniCalculation?.hasLdc() ?: false
    val awaitingAssessmentStatusDescription =
      referralStatusDescriptionRepository.getAwaitingAssessmentStatusDescription()

    val personalDetails: NDeliusPersonalDetails? = getPersonalDetails(findAndReferReferralDetails.personReference)

    val referralEntity = findAndReferReferralDetails.toReferralEntity(
      statusHistories = mutableListOf(),
      cohort = cohort,
      personalDetails = personalDetails,
      sentenceEndDate = sentenceEndDate,
    )

    log.info("Inserting referral for Intervention: '${referralEntity.interventionName}' and Crn: '${referralEntity.crn}' with cohort: $cohort")
    val referral = referralRepository.save(referralEntity)

    val statusHistoryEntity = ReferralStatusHistoryEntity(
      referral = referral,
      referralStatusDescription = awaitingAssessmentStatusDescription,
      startDate = LocalDateTime.now(),
      additionalDetails = null,
      createdBy = "SYSTEM",
    )
    referralEntity.statusHistories = mutableListOf(statusHistoryEntity)
    log.info("Inserting the default ReferralStatusHistory row for newly created Referral with id ${referral.id!!}")

    val referralLdcHistories =
      mutableSetOf(ReferralLdcHistoryEntity(hasLdc = hasLdc, referral = referralEntity, createdBy = "SYSTEM"))
    referralEntity.referralLdcHistories = referralLdcHistories

    personalDetails?.let {
      val referralReportingLocation = ReferralReportingLocationEntity(
        referral = referral,
        pduName = personalDetails.probationDeliveryUnit.description,
        reportingTeam = personalDetails.team.description,
        regionName = personalDetails.region.description,
      )
      referralReportingLocationRepository.save(referralReportingLocation)
      referralEntity.referralReportingLocationEntity = referralReportingLocation
    }

    log.info("Inserting referral for Intervention: '${referralEntity.interventionName}' and Crn: '${referralEntity.crn}' with cohort: $cohort and Ldc status: '$hasLdc'")
    referralRepository.save(referralEntity)
    return getReferralById(referral.id!!)
  }

  private fun getPni(findAndReferReferralDetails: FindAndReferReferralDetails): PniResponse? {
    var pniResponse: PniResponse? = null

    try {
      pniResponse = pniService.getPniCalculation(findAndReferReferralDetails.personReference)
    } catch (_: Exception) {
      log.info("Failure to retrieve PNI score for crn : ${findAndReferReferralDetails.personReference} falling back to defaults")
    }
    return pniResponse
  }

  fun getReferralById(referralId: UUID): ReferralEntity = referralRepository.findByIdOrNull(referralId) ?: let {
    log.error("Referral with id $referralId does not exist in database")
    throw NotFoundException("No Referral found for id: $referralId")
  }

  private fun getReferralAndEnsureSourcedFrom(referralId: UUID): ReferralEntity {
    log.info("getReferralAndEnsureSourcedFrom for $referralId")
    val referral = getReferralById(referralId)

    if (referral.eventId.isNullOrEmpty()) {
      log.error("Referral with id $referralId does not have an eventId")
      throw NotFoundException("Referral with id: $referralId exists, but has no eventId")
    }

    if (referral.sourcedFrom !== null) {
      log.info("Referral with id $referralId exists in database, and already has a sourcedFrom")
      return referral
    }

    when (ndeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, referral.eventId!!)) {
      is ClientResult.Success -> {
        log.info("Referral with id ${referral.id} appears to be sourced from a Requirement, saving Entity and continuing...")
        referral.sourcedFrom = ReferralEntitySourcedFrom.REQUIREMENT
        referralRepository.save(referral)
        return referral
      }

      else -> {
        log.info("Referral does not appear to come from Requirement, going to attempt to find a Licence Condition")
      }
    }

    when (ndeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, referral.eventId!!)) {
      is ClientResult.Success -> {
        log.info("Referral with id ${referral.id} appears to be sourced from a LicenceCondition, saving Entity and continuing...")
        referral.sourcedFrom = ReferralEntitySourcedFrom.LICENCE_CONDITION
        referralRepository.save(referral)
        return referral
      }

      else -> {
        log.info("Referral does not appear to come from Licence Condition, going to return null")
        throw NotFoundException("No LicenceCondition or Requirement found with id ${referral.eventId}")
      }
    }
  }

  private fun getRetRequirementOrLicenceCondition(referral: ReferralEntity): NDeliusCaseRequirementOrLicenceConditionResponse? {
    val referralIdString = referral.id.toString()
    val referralSourcedFrom = referral.sourcedFrom!!
    val eventId = referral.eventId!!

    if (referralSourcedFrom == ReferralEntitySourcedFrom.REQUIREMENT) {
      when (
        val response =
          ndeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, eventId)
      ) {
        is ClientResult.Success -> {
          log.info("...success! Found a Requirement for referral with ID: $referralIdString")
          return response.body
        }

        else -> {
          log.error("...failure, encountered an error while fetching Requirement for Referral with ID: $referralIdString from nDelius Integration API")
          throw NotFoundException("Could not fetch a Requirement with ID $referralIdString, for Referral with ID: $referralIdString")
        }
      }
    } else if (referralSourcedFrom == ReferralEntitySourcedFrom.LICENCE_CONDITION) {
      log.info("...attempting to retrieve a Licence Condition for Referral with ID: $referralIdString")

      when (val response = ndeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, eventId)) {
        is ClientResult.Success -> {
          log.info("...success! Found a Licence Condition for referral with ID: $referralIdString")
          return response.body
        }

        else -> {
          log.error("...failure, neither a Requirement or Licence Condition returned for Referral with ID $referralIdString")
          return null
        }
      }
    }

    log.info("Referral is neither sourced from Requirement or Licence Condition")
    return null
  }

  fun attemptToFindManagerForReferral(referralId: UUID): RequirementOrLicenceConditionManager? {
    val referral = this.getReferralAndEnsureSourcedFrom(referralId)

    val managerResponse = this.getRetRequirementOrLicenceCondition(referral) ?: return null

    return managerResponse.manager
  }

  fun attemptToFindNonPrimaryPdusForReferral(referralId: UUID): List<NDeliusApiProbationDeliveryUnitWithOfficeLocations>? {
    val referral = this.getReferralAndEnsureSourcedFrom(referralId)

    val ndeliusApiResponse = this.getRetRequirementOrLicenceCondition(referral) ?: return null

    return ndeliusApiResponse.probationDeliveryUnits
  }

  fun updateCohort(referral: ReferralEntity, cohort: OffenceCohort): Referral {
    referral.cohort = cohort
    val save = referralRepository.save(referral)
    return save.toApi()
  }

  fun updateStatus(
    referral: ReferralEntity,
    referralStatusDescriptionId: UUID,
    additionalDetails: String? = null,
    createdBy: String,
  ): StatusUpdateResponse {
    val incomingReferralStatusDescription =
      referralStatusDescriptionRepository.findByIdOrNull(referralStatusDescriptionId)

    if (incomingReferralStatusDescription == null) {
      log.warn("Unable to find Referral Status Description with ID $referralStatusDescriptionId")
      throw NotFoundException("Unable to find Referral Status Description with ID $referralStatusDescriptionId")
    }

    val currentReferralStatusHistory =
      referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referral.id!!)

    if (currentReferralStatusHistory == null) {
      log.error("Referral with id ${referral.id} does not have a current status history")
      throw BusinessException("Referral with id ${referral.id} does not have a current status history")
    }

    val transition = referralStatusTransitionRepository.findByFromStatusIdAndToStatusId(
      currentReferralStatusHistory.referralStatusDescription.id,
      incomingReferralStatusDescription.id,
    )

    var message = "${referral.personName}'s referral status is now ${incomingReferralStatusDescription.description}."
    val activeGroupMembership = programmeGroupMembershipService.getCurrentlyAllocatedGroup(referral)

    if (
      (transition == null || !transition.isContinuing) &&
      activeGroupMembership != null
    ) {
      programmeGroupMembershipService.deleteGroupMembershipForReferralAndGroup(referral, activeGroupMembership.programmeGroup, createdBy)
      if (incomingReferralStatusDescription.description != "Programme complete") {
        message =
          "${referral.personName}'s referral status is now ${incomingReferralStatusDescription.description}. They have been removed from group ${activeGroupMembership.programmeGroup.code}"
      }
    }

    val historyEntry = referralStatusHistoryRepository.save(
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = incomingReferralStatusDescription,
        additionalDetails = additionalDetails,
        createdBy = createdBy,
      ),
    )

    return StatusUpdateResponse(
      referralStatusHistory = historyEntry.toApi(),
      message = message,
    )
  }

  fun getStatusHistory(referralId: UUID): List<ReferralStatusHistory> = referralStatusHistoryRepository.findAllByReferralId(referralId).sortedBy { it.createdAt }.map { it.toApi() }

  fun updateReferralDetails(
    referral: ReferralEntity,
    personalDetails: NDeliusPersonalDetails,
    sentenceEndDate: LocalDate?,
  ) {
    // If there is already a row in the db then update it otherwise create a new one
    val referralReportingLocation = referralReportingLocationRepository.findByReferralId(referral.id)
      ?.apply {
        pduName = personalDetails.probationDeliveryUnit.description
        reportingTeam = personalDetails.team.description
      }
      ?: ReferralReportingLocationEntity(
        referral = referral,
        pduName = personalDetails.probationDeliveryUnit.description,
        reportingTeam = personalDetails.team.description,
        regionName = personalDetails.region.description,
      )

    val savedEntity = referralReportingLocationRepository.save(referralReportingLocation)
    // Update our referral entity with details fetched from nDelius
    referral.referralReportingLocationEntity = savedEntity
    referral.personName = personalDetails.name.getNameAsString()
    referral.sex = personalDetails.sex.description
    referral.dateOfBirth = personalDetails.dateOfBirth.toLocalDate()
    referral.sentenceEndDate = sentenceEndDate
    referralRepository.save(referral)
  }

  fun getCurrentStatusHistory(referral: ReferralEntity): ReferralStatusHistoryEntity? = referral.statusHistories.maxByOrNull { it.createdAt }

  private fun getPersonalDetails(crn: String) = when (val result = ndeliusIntegrationApiClient.getPersonalDetails(crn)) {
    is ClientResult.Success -> result.body
    else -> null
  }
}
