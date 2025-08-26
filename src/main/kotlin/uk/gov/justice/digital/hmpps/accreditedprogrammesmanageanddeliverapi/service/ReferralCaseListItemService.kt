package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getReferralCaseListItemSpecification

@Service
class ReferralCaseListItemService(
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
  private val referralRepository: ReferralRepository,
  private val ldcNeedsService: LdcNeedsService,
) {
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    crnOrPersonName: String?,
    cohort: String?,
    status: String?,
  ): Page<ReferralCaseListItem> {
    val specification = getReferralCaseListItemSpecification(openOrClosed, crnOrPersonName, cohort, status)

    return referralCaseListItemRepository.findAll(specification, pageable).map { entity ->
      // Load the full ReferralEntity so we can resolve LDC needs consistently
      val referral = referralRepository.findById(entity.referralId)
        .orElseThrow { IllegalStateException("Referral not found for id=${entity.referralId}") }

      val finalLdcNeeds = ldcNeedsService.resolveLdcNeeds(referral!!)

      entity.toApi(finalLdcNeeds)
    }
  }
}
