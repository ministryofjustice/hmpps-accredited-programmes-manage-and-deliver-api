package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getReferralCaseListItemSpecification
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class ReferralCaseListItemService(
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
  private val serviceUserService: ServiceUserService,
  private val authenticationHolder: HmppsAuthenticationHolder,
) {
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    crnOrPersonName: String?,
    cohort: String?,
    status: String?,
  ): Page<ReferralCaseListItem> {
    val specification = getReferralCaseListItemSpecification(openOrClosed, crnOrPersonName, cohort, status)
    val allItems = referralCaseListItemRepository.findAll(specification) // List<Entity>

    val username = authenticationHolder.username
    if (username == null) {
      throw AuthenticationCredentialsNotFoundException("No authenticated user found")
    }
    val crns = allItems.map { it.crn }
    val allowedCrns = serviceUserService.getAccessibleOffenders(username, crns)

    val filtered = allItems
      .filter { it.crn in allowedCrns }
      .map { it.toApi() }

    // apply paging AFTER filtering
    val start = pageable.offset.toInt()
    val end = (start + pageable.pageSize).coerceAtMost(filtered.size)
    val pageContent = if (start <= end) filtered.subList(start, end) else emptyList()

    return PageImpl(pageContent, pageable, filtered.size.toLong())
  }
}
