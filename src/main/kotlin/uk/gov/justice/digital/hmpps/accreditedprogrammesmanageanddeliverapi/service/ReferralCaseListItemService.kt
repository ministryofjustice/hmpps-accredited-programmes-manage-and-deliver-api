package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.LocationFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.ReferralCaseListItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.StatusFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getReferralCaseListItemSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withAllowedCrns
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder

@Service
class ReferralCaseListItemService(
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
  private val serviceUserService: ServiceUserService,
  private val authenticationHolder: HmppsAuthenticationHolder,
  private val referralStatusService: ReferralStatusService,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
) {
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    crnOrPersonName: String?,
    cohort: String?,
    status: String?,
    pdu: String?,
    reportingTeam: List<String>?,
  ): Page<ReferralCaseListItem> {
    val username = authenticationHolder.username
      ?: throw AuthenticationCredentialsNotFoundException("No authenticated user found")

    val possibleStatuses = referralStatusService.getOpenOrClosedStatusesDescriptions(openOrClosed)

    val baseSpec = getReferralCaseListItemSpecification(possibleStatuses, crnOrPersonName, cohort, status, pdu, reportingTeam)
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

  fun getCaseListFilterData(openOrClosed: OpenOrClosed): CaseListFilterValues {
    val allStatuses = referralStatusService.getAllStatuses()

    val (closed, open) = allStatuses.partition { it.isClosed }

    val statusesToCount = if (openOrClosed == OpenOrClosed.OPEN) closed else open
    val otherReferralsCount = referralCaseListItemRepository.countAllByStatusIn(statusesToCount.map { it.description })
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val pdusWithReportingTeams = referralReportingLocations.groupBy { it.pduName }
      .map { (pduName, reportingTeams) ->
        LocationFilterValues(pduName = pduName, reportingTeams = reportingTeams.map { it.reportingTeam }.distinct())
      }

    val statusFilterValues = StatusFilterValues(
      open = open.map { it.description },
      closed = closed.map { it.description },
    )

    return CaseListFilterValues(
      statusFilterValues = statusFilterValues,
      locationFilterValues = pdusWithReportingTeams,
      otherReferralsCount = otherReferralsCount,
    )
  }
}
