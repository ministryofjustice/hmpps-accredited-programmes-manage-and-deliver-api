package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusInfo
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toSuggestedStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusDescriptionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import java.util.UUID

@Service
@Transactional
class ReferralStatusService(
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
  private val referralStatusDescriptionRepository: ReferralStatusDescriptionRepository,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
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

    return ReferralStatusInfo(
      newStatus = ReferralStatusInfo.Status.fromDisplayName(statusHistory.referralStatusDescription.description),
      sourcedFromEntityType = sourcedFrom,
      sourcedFromEntityId = eventId.toLong(),
      notes = statusHistory.additionalDetails,
    )
  }
}
