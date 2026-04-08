package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.eventDetails.ReferralCompletionData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toSuggestedStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionAttendanceRepository
import java.util.UUID

@Service
@Transactional
class ReferralStatusService(
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val referralRepository: ReferralRepository,
  private val sessionAttendanceRepository: SessionAttendanceRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun getStatusTransitionsForReferral(referralId: UUID): ReferralStatusTransitions? {
    val currentStatus =
      referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) ?: return null
    val availableStatusesToDisplay =
      referralStatusTransitionRepository.findByFromStatusIdAndIsVisibleTrueOrderByPriorityAsc(currentStatus.referralStatusDescription.id)

    val statuses = availableStatusesToDisplay.map { it.toStatus.toApi(it.description) }

    // There will only ever be one isSuggested is true transition from each status
    val suggestedStatus =
      referralStatusTransitionRepository.findFirstByFromStatusIdAndIsSuggestedTrue(currentStatus.referralStatusDescription.id)

    val currentGroupMembership = programmeGroupMembershipRepository.findCurrentGroupByReferralId(referralId)

    return ReferralStatusTransitions(
      currentStatus = currentStatus.toCurrentStatus(),
      availableStatuses = statuses,
      suggestedStatus = suggestedStatus?.toSuggestedStatus(),
      currentGroupDetails = currentGroupMembership?.toCurrentGroupDetails(),
    )
  }

  fun getAllStatuses(): List<ReferralStatusDescriptionEntity> = referralStatusDescriptionRepository.findAll().sortedBy { it.description }

  fun getOpenOrClosedStatuses(openOrClosed: OpenOrClosed): List<ReferralStatusDescriptionEntity> {
    val isClosed = openOrClosed === OpenOrClosed.CLOSED
    return referralStatusDescriptionRepository.findAllByIsClosed(isClosed)
  }

  fun getOpenOrClosedStatusesDescriptions(openOrClosed: OpenOrClosed) = getOpenOrClosedStatuses(openOrClosed).map { it.description }

  fun getStatusChangeDetailsForReferral(referralId: UUID): ReferralStatusInfo {
    val statusHistory =
      referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId)
        ?: throw NotFoundException("Referral with id: $referralId could not be found.")

    val referral = statusHistory.referral
    val sourcedFrom = referral.sourcedFrom
    val eventId = referral.eventId

    requireNotNull(sourcedFrom) { "SourcedFrom must not be null" }
    requireNotNull(eventId) { "EventId must not be null" }

    // Map our M&D status description to a more readable string
    // e.g. Awaiting allocation -> The person is ready to be allocated to a programme group.
    val statusInfo =
      ReferralStatusInfo.Status.fromDisplayName(statusHistory.referralStatusDescription.description)

    return ReferralStatusInfo(
      newStatus = statusInfo,
      sourcedFromEntityType = sourcedFrom,
      sourcedFromEntityId = eventId.toLong(),
      notes = statusHistory.additionalDetails,
      description = statusInfo.description,
    )
  }

  fun getCompletionDataForReferral(referralId: UUID): ReferralCompletionData {
    val referral = referralRepository.findByIdOrNull(referralId)
      ?: throw NotFoundException("Referral with id $referralId not found")

    val eventId = requireNotNull(referral.eventId) { "EventId must not be null" }

    val currentGroupMembership = referral.programmeGroupMemberships.maxByOrNull { it.createdAt }
      ?: throw NotFoundException("No group membership found for referral $referralId")

    val postProgrammeReviewSession = currentGroupMembership.programmeGroup.sessions
      .find { it.moduleSessionTemplate.module.isPostProgrammeModule() && !it.isPlaceholder }
      ?: throw NotFoundException("No post-programme review session found for referral $referralId")

    val attendance = sessionAttendanceRepository.findFirstBySessionIdAndGroupMembershipIdOrderByRecordedAtDesc(
      sessionId = postProgrammeReviewSession.id!!,
      groupMembershipId = currentGroupMembership.id!!,
    ) ?: throw NotFoundException("No attendance record found for referral $referralId in post-programme review session")

    val attended = attendance.outcomeType.attendance == true
    val complied = attendance.outcomeType.compliant

    if (!attended || !complied) {
      throw BusinessException("Referral $referralId did not attend or comply with the post-programme review session")
    }

    val completedAt = requireNotNull(attendance.recordedAt) {
      "Attendance record for referral $referralId does not have a recordedAt timestamp"
    }

    return ReferralCompletionData(
      requirementId = eventId,
      requirementCompletedAt = completedAt,
    )
  }
}
