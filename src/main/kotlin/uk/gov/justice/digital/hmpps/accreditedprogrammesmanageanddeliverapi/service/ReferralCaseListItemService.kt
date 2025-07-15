package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.PagedReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toPagedReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository

@Service
class ReferralCaseListItemService(private val referralCaseListItemRepository: ReferralCaseListItemRepository) {
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
  ): PagedReferralCaseListItem = referralCaseListItemRepository.findAll(pageable).map { it.toApi() }.toPagedReferralCaseListItem()
}
