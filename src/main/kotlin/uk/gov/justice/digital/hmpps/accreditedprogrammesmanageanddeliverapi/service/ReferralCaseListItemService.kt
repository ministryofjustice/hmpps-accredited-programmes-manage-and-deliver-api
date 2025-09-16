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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withAllowedCrns
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
    val username = authenticationHolder.username
      ?: throw AuthenticationCredentialsNotFoundException("No authenticated user found")

    val baseSpec = getReferralCaseListItemSpecification(openOrClosed, crnOrPersonName, cohort, status)
    val crns = referralCaseListItemRepository.findAllCrns(baseSpec)

    if (crns.isEmpty()) {
      return PageImpl(emptyList(), pageable, 0)
    }

    val allowedCrns = serviceUserService.getAccessibleOffenders(username, crns)

    if (allowedCrns.isEmpty()) {
      return PageImpl(emptyList(), pageable, 0)
    }

    val restrictedSpec = withAllowedCrns(baseSpec, allowedCrns)
    val pagedEntities = referralCaseListItemRepository.findAll(restrictedSpec, pageable)

    return pagedEntities.map { it.toApi() }
  }
}
