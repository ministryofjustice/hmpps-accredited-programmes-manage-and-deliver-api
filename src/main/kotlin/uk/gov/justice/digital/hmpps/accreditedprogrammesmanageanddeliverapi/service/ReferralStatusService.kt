package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralStatusFormData
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralStatusTransitionRepository
import java.util.UUID

@Service
@Transactional
class ReferralStatusService(
  private val referralStatusTransitionRepository: ReferralStatusTransitionRepository,
  private val referralStatusHistoryRepository: ReferralStatusHistoryRepository,
) {

  fun getReferralStatusTransitionsForReferralStatusDescriptionId(fromStatusId: UUID): List<ReferralStatus> = referralStatusTransitionRepository.findByFromStatusId(fromStatusId)
    .map { it.toStatus.toApi(it.description!!) }

  fun getReferralStatusFormDataFromReferralId(referralId: UUID): ReferralStatusFormData? {
    val currentStatus = referralStatusHistoryRepository.findFirstByReferralIdOrderByCreatedAtDesc(referralId) ?: return null
    val availableStatuses = referralStatusTransitionRepository.findByFromStatusId(currentStatus.referralStatusDescription.id).map { it.toStatus.toApi(it.description) }

    return ReferralStatusFormData(currentStatus.toApi(), availableStatuses)
  }
}
