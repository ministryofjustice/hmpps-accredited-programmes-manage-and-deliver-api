package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusTransitions
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toCurrentStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
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
) {

  fun getStatusTransitionsForReferral(referralId: UUID): ReferralStatusTransitions? {
    val currentStatus =
      referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) ?: return null
    val availableStatuses =
      referralStatusTransitionRepository.findByFromStatusIdOrderByPriorityAsc(currentStatus.referralStatusDescription.id)
        .map { it.toStatus.toApi(it.description) }

    return ReferralStatusTransitions(currentStatus.toCurrentStatus(), availableStatuses)
  }

  fun getAllStatuses(): List<ReferralStatusDescriptionEntity> = referralStatusDescriptionRepository.findAll().sortedBy { it.description }

  fun getOpenOrClosedStatuses(openOrClosed: OpenOrClosed): List<ReferralStatusDescriptionEntity> {
    val isClosed = openOrClosed === OpenOrClosed.CLOSED
    return referralStatusDescriptionRepository.findAllByIsClosed(isClosed)
  }

  fun getOpenOrClosedStatusesDescriptions(openOrClosed: OpenOrClosed) = getOpenOrClosedStatuses(openOrClosed).map { it.description }
}
