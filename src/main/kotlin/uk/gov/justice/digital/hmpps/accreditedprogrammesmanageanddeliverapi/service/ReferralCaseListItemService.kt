package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.controller.OpenOrClosed
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.LocationFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.OffenceCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.CaseListReferrals
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.StatusFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.caseList.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralCaseListItemViewEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralCaseListItemRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getReferralCaseListItemSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withAllowedCrns
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withRegionNames
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReferralStatusUtils

@Service
class ReferralCaseListItemService(
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
  private val userService: UserService,
  private val referralStatusService: ReferralStatusService,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,

) {
  private val log = LoggerFactory.getLogger(this::class.java)
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    username: String,
    crnOrPersonName: String?,
    cohort: ProgrammeGroupCohort?,
    status: String?,
    pdus: List<String>?,
    reportingTeams: List<String>?,
  ): CaseListReferrals {
    val (offenceType, hasLdc) = cohort?.let { ProgrammeGroupCohort.toOffenceTypeAndLdc(it) }
      ?: (null to null)

    val userRegionNames = userService.getUserRegionNames(username)

    val referralsToReturn = getReferralCaseList(
      pageable = pageable,
      openOrClosed = openOrClosed,
      username = username,
      crnOrPersonName = crnOrPersonName,
      offenceCohort = offenceType,
      hasLdc = hasLdc,
      status = ReferralStatusUtils.unformatStatus(status),
      pdus = pdus,
      reportingTeams = reportingTeams,
    ).map { it.toApi() }

    val otherTabCount = getReferralCaseList(
      pageable = pageable,
      openOrClosed = if (openOrClosed == OpenOrClosed.OPEN) OpenOrClosed.CLOSED else OpenOrClosed.OPEN,
      username = username,
      crnOrPersonName = crnOrPersonName,
      offenceCohort = offenceType,
      hasLdc = hasLdc,
      status = status,
      pdus = pdus,
      reportingTeams = reportingTeams,
    ).totalElements

    return CaseListReferrals(referralsToReturn, otherTabCount.toInt(), this.getCaseListFilterData(userRegionNames))
  }

  private fun getReferralCaseList(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    username: String,
    crnOrPersonName: String?,
    offenceCohort: OffenceCohort?,
    hasLdc: Boolean?,
    status: String?,
    pdus: List<String>?,
    reportingTeams: List<String>?,
  ): Page<ReferralCaseListItemViewEntity> {
    val possibleStatuses = referralStatusService.getOpenOrClosedStatusesDescriptions(openOrClosed)

    val baseSpec =
      getReferralCaseListItemSpecification(
        possibleStatuses = possibleStatuses,
        crnOrPersonName = crnOrPersonName,
        offenceCohort = offenceCohort,
        hasLdc = hasLdc,
        status = status,
        pdus = pdus,
        reportingTeams = reportingTeams,
      )

    val userRegions = userService.getUserRegionNames(username)
    val specWithRegions = if (userRegions.isEmpty()) {
      log.warn("No regions found for user: $username. Returning empty list for ReferralCaseList.")
      return PageImpl(emptyList(), pageable, 0)
    } else {
      withRegionNames(baseSpec, userRegions)
    }
    val crns = referralCaseListItemRepository.findAllCrns(specWithRegions)

    if (crns.isEmpty()) {
      log.warn("No CRNs found for user: $username. Returning empty list for ReferralCaseList.")
      return PageImpl(emptyList(), pageable, 0)
    }

    // Batch LAO checks in chunks of 500 (hard API limit) across ALL CRNs
    val allowedCrns = crns
      .chunked(500)
      .flatMap { userService.getAccessibleOffenders(username, it) }
      .toSet()

    if (allowedCrns.isEmpty()) {
      log.warn("No CRNs are allowed for user: $username. Returning empty list for ReferralCaseList.")
      return PageImpl(emptyList(), pageable, 0)
    }

    val restrictedSpec = withAllowedCrns(specWithRegions, allowedCrns)
    val totalAllowedCount = referralCaseListItemRepository.count(restrictedSpec)
    val caseListReferrals = referralCaseListItemRepository.findAll(restrictedSpec, pageable)

    if (caseListReferrals.totalElements < 50) log.warn("Only ${caseListReferrals.totalElements} out of ${pageable.pageSize} referrals returned due to Limited Access Offender check ")
    return PageImpl(caseListReferrals.content, pageable, totalAllowedCount)
  }

  fun getCaseListFilterData(userRegionNames: List<String>): CaseListFilterValues {
    val allStatuses = referralStatusService.getAllStatuses()

    val (closed, open) = allStatuses.partition { it.isClosed }

    val referralReportingLocations = if (userRegionNames.isEmpty()) {
      emptyList()
    } else {
      referralReportingLocationRepository.getPdusAndReportingTeamsByRegions(userRegionNames)
    }
    val pdusWithReportingTeams = referralReportingLocations.groupBy { it.pduName }
      .map { (pduName, reportingTeams) ->
        LocationFilterValues(pduName = pduName, reportingTeams = reportingTeams.map { it.reportingTeam }.distinct())
      }
      .sortedBy { it.pduName }

    // For this instance of displaying the status' on the front end, the description of "Breach (non-attendance)" needs to be changed.
    val openDescriptions = ReferralStatusUtils.sortStatuses(open.map { ReferralStatusUtils.formatStatus(it.description) })

    val statusFilterValues = StatusFilterValues(
      open = openDescriptions,
      closed = ReferralStatusUtils.sortStatuses(closed.map { it.description }),
    )

    return CaseListFilterValues(
      statusFilterValues = statusFilterValues,
      locationFilterValues = pdusWithReportingTeams,
      ProgrammeGroupCohort.entries.map { it.label },
    )
  }
}
