package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralMotivationBackgroundAndNonAssociations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralMotivationBackgroundAndNonAssociationsRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import java.util.UUID

@Service
@Transactional
class GroupAllocationNotesService(
  private val referralMotivationBackgroundAndNonAssociationsRepository: ReferralMotivationBackgroundAndNonAssociationsRepository,
  private val referralRepository: ReferralRepository,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getReferralMotivationBackgroundAndNonAssociationsByReferralId(referralId: UUID): ReferralMotivationBackgroundAndNonAssociations {
    val motivationBackgroundAndNonAssociations = referralMotivationBackgroundAndNonAssociationsRepository.findByReferralId(referralId)
      ?: throw NotFoundException("No motivation, background and non-associations information found for referral id: $referralId")

    return ReferralMotivationBackgroundAndNonAssociations.toApi(motivationBackgroundAndNonAssociations)
  }
}
