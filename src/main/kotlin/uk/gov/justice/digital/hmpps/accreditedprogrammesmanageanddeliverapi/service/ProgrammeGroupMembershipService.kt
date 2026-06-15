package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.RemoveFromGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntitySourcedFrom
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.event.listener.ReferralStatusUpdateEvent
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.ASSIGN_REFERRAL_TO_GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.UserActivityType.REMOVE_REFERRAL_FROM_GROUP
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupMembershipService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val referralRepository: ReferralRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val scheduleService: ScheduleService,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val telemetryClient: TelemetryClient,
  private val applicationEventPublisher: ApplicationEventPublisher,
  private val clock: Clock,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun allocateReferralToGroup(
    referralId: UUID,
    groupId: UUID,
    allocatedToGroupBy: String,
    additionalDetails: String,
  ): ReferralEntity {
    val referral = referralRepository.findByIdOrNull(referralId)
      ?: throw NotFoundException("Referral with id $referralId not found")

    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val latestStatus = referralStatusDescriptionRepository.findMostRecentStatusByReferralId(referralId)
    if (latestStatus?.isClosed == true) {
      throw BusinessException("Cannot assign referral to group as referral with id ${referral.id} is in a closed state")
    }

    getCurrentlyAllocatedGroup(referral)
      ?.let { throw ConflictException("Referral with id ${referral.id} is already allocated to a group: ${it.programmeGroup.code}") }

    // Validate the referral's requirement/licence condition exists in nDelius before mutating state.
    // This is an intentional extra GET call on every allocation to fail fast with a clear error message
    // rather than getting a cryptic 400 from POST /appointments after state has been partially mutated.
    validateReferralSentenceDataExistsInNDelius(referral)

    log.info("Adding referral with id: $referralId to group with id: $groupId and groupCode: ${group.code}")

    referral.programmeGroupMemberships.add(ProgrammeGroupMembershipEntity(referral = referral, programmeGroup = group))

    val statusHistory =
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = referralStatusDescriptionRepository.getScheduledStatusDescription(),
        additionalDetails = additionalDetails,
        createdBy = allocatedToGroupBy,
      )

    referral.statusHistories.add(statusHistory)
    val currentGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)
      ?: throw NotFoundException("No group membership found for referral $referralId")

    // Filter out individual sessions
    val coreGroupSessions =
      group.sessions.filter { it.sessionType == SessionType.GROUP }

    val newAttendees = coreGroupSessions.map { session ->
      val attendeeEntity = AttendeeEntity(
        referral = currentGroupMembership.referral,
        session = session,
      )
      session.attendees.add(attendeeEntity)
      attendeeEntity
    }

    // Create appointments in NDelius for each session object for future session only
    val now = LocalDateTime.now(clock)
    scheduleService.createNdeliusAppointmentsForSessions(newAttendees.filter { it.session.startsAt > now })

    val savedReferral = referralRepository.save(referral)

    applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId))

    telemetryClient.logToAppInsights(
      "Referral.allocate-to-group.success",
      mapOf(
        "activityType" to ASSIGN_REFERRAL_TO_GROUP.name,
        "regionName" to (savedReferral.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (savedReferral.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to group.deliveryLocationName,
      ),
    )

    return savedReferral
  }

  fun getCurrentlyAllocatedGroup(referral: ReferralEntity): ProgrammeGroupMembershipEntity? {
    val membership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referral.id!!) ?: return null

    if (membership.deletedAt == null) return membership

    return null
  }

  fun getActiveGroupMemberships(groupId: UUID): List<ProgrammeGroupMembershipEntity> = programmeGroupMembershipRepository.findAllActiveByProgrammeGroupId(groupId)

  fun removeReferralFromGroup(
    referralId: UUID,
    groupId: UUID,
    removedFromGroupBy: String,
    removeFromGroupRequest: RemoveFromGroupRequest,
  ): RemoveFromGroupResponse {
    log.info("Attempting to remove Referral with id: $referralId from group with id: $groupId...")

    val group = (
      programmeGroupRepository.findByIdOrNull(groupId)
        ?: throw NotFoundException("Group with id $groupId not found")
      )

    val referral = referralRepository.findByIdOrNull(referralId)
      ?: throw NotFoundException("Referral with id $referralId not found")

    deleteGroupMembershipForReferralAndGroup(referral, group, removedFromGroupBy)

    val desiredStatus =
      referralStatusDescriptionRepository.findByIdOrNull(removeFromGroupRequest.referralStatusDescriptionId)
        ?: throw NotFoundException("No Referral Status Description found for id: ${removeFromGroupRequest.referralStatusDescriptionId}")

    val statusHistory =
      ReferralStatusHistoryEntity(
        referral = referral,
        referralStatusDescription = desiredStatus,
        additionalDetails = removeFromGroupRequest.additionalDetails,
        createdBy = removedFromGroupBy,
      )

    referral.statusHistories.add(statusHistory)
    val savedReferral = referralRepository.save(referral)
    applicationEventPublisher.publishEvent(ReferralStatusUpdateEvent(referralId))

    telemetryClient.logToAppInsights(
      "Referral.remove-from-group.success",
      mapOf(
        "activityType" to REMOVE_REFERRAL_FROM_GROUP.name,
        "regionName" to (savedReferral.referralReportingLocation?.regionName ?: ""),
        "deliveryUnitCode" to (savedReferral.referralReportingLocation?.pduName ?: ""),
        "deliveryLocation" to group.deliveryLocationName,
      ),
    )

    return RemoveFromGroupResponse(
      message = "${savedReferral.personName} was removed from this group. Their referral status is now ${desiredStatus.description}",
    )
  }

  fun deleteGroupMembershipForReferralAndGroup(
    referral: ReferralEntity,
    group: ProgrammeGroupEntity,
    deletedByUsername: String,
  ): ProgrammeGroupEntity {
    val groupMembership =
      programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referral.id!!, group.id!!)
        ?: throw NotFoundException("No active Membership found for Referral (${referral.id}) and Group (${group.id})")

    val now = LocalDateTime.now()
    //  Group membership is soft deleted
    groupMembership.deletedAt = now
    groupMembership.deletedByUsername = deletedByUsername

    val futureSessions = group.sessions.filter { it.startsAt > now }

    // Remove the PoP from the list of attendees for any future sessions of the group they have been removed from
    val attendeesToRemove = futureSessions.flatMap { session ->
      session.attendees.filter { it.referral.id == referral.id }
    }

    val futureNdeliusAppointmentsToRemove = futureSessions.flatMap { session ->
      session.ndeliusAppointments.filter { it.referral.id == referral.id }
    }
    // Ensure future session appointments for the PoP are also removed from nDelius
    scheduleService.removeNDeliusAppointments(futureNdeliusAppointmentsToRemove, futureSessions)

    // remove the attendees from the sessions
    attendeesToRemove.forEach { it.session.attendees.remove(it) }

    val sessionsToRemove =
      futureSessions.filter { (it.sessionType == SessionType.ONE_TO_ONE && !it.isPlaceholder) || (it.isCatchup && it.attendees.isEmpty()) }
    group.sessions.removeAll(sessionsToRemove.toSet())

    log.info("...Successfully found Referral (${referral.id}), Group (${group.id}), and Membership (${groupMembership.id}) to remove")
    return programmeGroupRepository.save(group)
  }

  /**
   * Validates that the referral's requirement or licence condition still exists in nDelius
   * before attempting to create appointments. This prevents 400 errors from nDelius when
   * the sentence data is stale (e.g., after a transfer or sentence termination).
   */
  private fun validateReferralSentenceDataExistsInNDelius(referral: ReferralEntity) {
    val eventId = referral.eventId
    if (eventId == null) {
      log.error("Cannot validate nDelius sentence data: eventId is null for referral ${referral.id}")
      throw BusinessException(
        "Cannot allocate referral to group: the referral has no associated requirement or licence condition ID. " +
          "Please update the referral's sentence data before allocating to a group.",
      )
    }

    val sourcedFrom = referral.sourcedFrom
    if (sourcedFrom == null) {
      log.error("Cannot validate nDelius sentence data: sourcedFrom is null for referral ${referral.id}")
      throw BusinessException(
        "Cannot allocate referral to group: the referral's sentence source type is not set. " +
          "Please update the referral's sentence data before allocating to a group.",
      )
    }

    val result = when (sourcedFrom) {
      ReferralEntitySourcedFrom.REQUIREMENT ->
        nDeliusIntegrationApiClient.getRequirementManagerDetails(referral.crn, eventId)

      ReferralEntitySourcedFrom.LICENCE_CONDITION ->
        nDeliusIntegrationApiClient.getLicenceConditionManagerDetails(referral.crn, eventId)
    }

    val sourceType = when (sourcedFrom) {
      ReferralEntitySourcedFrom.REQUIREMENT -> "requirement"
      ReferralEntitySourcedFrom.LICENCE_CONDITION -> "licence condition"
    }

    when (result) {
      is ClientResult.Success -> {
        log.debug("nDelius validation passed for referral ${referral.id}: ${sourcedFrom.name} $eventId exists")
      }

      is ClientResult.Failure.StatusCode -> {
        log.error(
          "nDelius validation failed for referral ${referral.id}: $sourceType with ID $eventId " +
            "does not exist in nDelius for CRN ${referral.crn} (status: ${result.status})",
          result.toException(),
        )
        telemetryClient.logToAppInsights(
          "Referral.allocate-to-group.ndelius-validation-failure",
          mapOf(
            "referralId" to referral.id.toString(),
            "crn" to referral.crn,
            "sourcedFrom" to sourcedFrom.name,
            "eventId" to eventId,
            "statusCode" to result.status.toString(),
          ),
        )
        throw BusinessException(
          "Cannot allocate referral to group: the $sourceType linked to this referral " +
            "no longer exists in nDelius. The sentence data may be stale following a transfer or termination. " +
            "Please contact your admin to update the referral's sentence data.",
        )
      }

      is ClientResult.Failure.Other -> {
        log.error(
          "nDelius validation failed for referral ${referral.id}: unable to reach nDelius " +
            "to verify $sourceType with ID $eventId for CRN ${referral.crn}",
          result.exception,
        )
        telemetryClient.logToAppInsights(
          "Referral.allocate-to-group.ndelius-validation-failure",
          mapOf(
            "referralId" to referral.id.toString(),
            "crn" to referral.crn,
            "sourcedFrom" to sourcedFrom.name,
            "eventId" to eventId,
            "errorType" to "network",
            "errorMessage" to (result.exception.message ?: "unknown"),
          ),
        )
        throw BusinessException(
          "Cannot allocate referral to group: unable to reach nDelius to verify sentence data. " +
            "Please try again later.",
        )
      }
    }
  }
}
