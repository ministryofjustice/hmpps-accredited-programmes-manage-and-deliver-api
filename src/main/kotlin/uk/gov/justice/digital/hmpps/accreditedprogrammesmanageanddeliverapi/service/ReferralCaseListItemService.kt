package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListReferrals
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.LocationFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.StatusFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
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
    reportingTeams: List<String>?,
  ): CaseListReferrals {
    val referralsToReturn = getReferralCaseList(
      pageable = pageable,
      openOrClosed = openOrClosed,
      crnOrPersonName = crnOrPersonName,
      cohort = cohort,
      status = status,
      pdu = pdu,
      reportingTeams = reportingTeams,
    ).map { it.toApi() }

    val otherTabCount = getReferralCaseList(
      pageable = pageable,
      openOrClosed = if (openOrClosed == OpenOrClosed.OPEN) OpenOrClosed.CLOSED else OpenOrClosed.OPEN,
      crnOrPersonName = crnOrPersonName,
      cohort = cohort,
      status = status,
      pdu = pdu,
      reportingTeams = reportingTeams,
    ).totalElements

    return CaseListReferrals(referralsToReturn, otherTabCount.toInt())
  }

  private fun getReferralCaseList(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    crnOrPersonName: String?,
    cohort: String?,
    status: String?,
    pdu: String?,
    reportingTeams: List<String>?,
  ): Page<ReferralCaseListItemViewEntity> {
    val username = authenticationHolder.username
      ?: throw AuthenticationCredentialsNotFoundException("No authenticated user found")

    val possibleStatuses = referralStatusService.getOpenOrClosedStatusesDescriptions(openOrClosed)

    val baseSpec =
      getReferralCaseListItemSpecification(possibleStatuses, crnOrPersonName, cohort, status, pdu, reportingTeams)
    val crns = referralCaseListItemRepository.findAllCrns(baseSpec)

    if (crns.isEmpty()) {
      return PageImpl(emptyList(), pageable, 0)
    }

    val allowedCrns = serviceUserService.getAccessibleOffenders(username, crns)

    if (allowedCrns.isEmpty()) {
      return PageImpl(emptyList(), pageable, 0)
    }

    val restrictedSpec = withAllowedCrns(baseSpec, allowedCrns)
    return referralCaseListItemRepository.findAll(restrictedSpec, pageable)
  }

  fun getCaseListFilterData(): CaseListFilterValues {
    val allStatuses = referralStatusService.getAllStatuses()

    val (closed, open) = allStatuses.partition { it.isClosed }

    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val pdusWithReportingTeams = referralReportingLocations.groupBy { it.pduName }
      .map { (pduName, reportingTeams) ->
        LocationFilterValues(pduName = pduName, reportingTeams = reportingTeams.map { it.reportingTeam }.distinct())
      }

    // For this instance of displaying the status' on the front end, the description of "Breach (non-attendance)" needs to be changed.
    val openDescriptions = open.map {
      if (it.description == "Breach (non-attendance)") "Breach" else it.description
    }

    val statusFilterValues = StatusFilterValues(
      open = openDescriptions,
      closed = closed.map { it.description },
    )

    return CaseListFilterValues(
      statusFilterValues = statusFilterValues,
      locationFilterValues = pdusWithReportingTeams,
    )
  }
}
