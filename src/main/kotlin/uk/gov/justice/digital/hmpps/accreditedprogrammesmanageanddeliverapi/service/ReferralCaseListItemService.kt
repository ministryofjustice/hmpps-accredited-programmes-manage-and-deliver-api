package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withCrns
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.withRegionNames
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service.UserAccessService.Access
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.ReferralStatusUtils

@Service
class ReferralCaseListItemService(
  private val referralCaseListItemRepository: ReferralCaseListItemRepository,
  private val userService: UserService,
  private val referralStatusService: ReferralStatusService,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  @param:Value($$"${app.features.lao-access-check-enabled}")
  private val limitedAccessOffenderCheckEnabled: Boolean,
  @param:Value($$"${app.features.exclusion-access-check-enabled}")
  private val exclusionAccessCheckEnabled: Boolean,
  private val userAccessService: UserAccessService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    username: String,
    caseReferenceNumberOrPersonName: String?,
    cohorts: List<ProgrammeGroupCohort>?,
    statuses: List<String>?,
    sexes: List<String>?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): CaseListReferrals {
    val offenceCohortAndLdcPairs = cohorts?.mapNotNull { cohort ->
      ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
    } ?: emptyList()

    val isFilteredCaseList =
      isFilterApplied(caseReferenceNumberOrPersonName, cohorts, sexes, probationDeliveryUnits, reportingTeams)
    val userRegionNames = userService.getUserRegionNames(username)

    // Normalise the status filters once so both the main query and the otherTabCount query
    // receive the same DB-compatible values (e.g. "Breach" -> "Breach (non-attendance)").
    val normalisedStatuses = statuses?.mapNotNull { ReferralStatusUtils.unformatStatus(it) }

    val referralsPage = getReferralCaseList(
      pageable = pageable,
      openOrClosed = openOrClosed,
      username = username,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      offenceCohortAndLdcPairs = offenceCohortAndLdcPairs,
      statuses = normalisedStatuses,
      sexes = sexes,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    )

    // Fetch Limited Access Offender (LAO) status for all distinct case reference numbers (CRNs)
    var limitedAccessOffenderAccessMap: Map<String, Access>? = null
    if (limitedAccessOffenderCheckEnabled) {
      val caseReferenceNumbers = referralsPage.content.map { it.crn }.distinct()
      limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, caseReferenceNumbers)
    }

    val referralCaseListItems = referralsPage.content.filter { referral ->
      if (exclusionAccessCheckEnabled && isFilteredCaseList) {
        val crnMatchesSearch = !caseReferenceNumberOrPersonName.isNullOrEmpty() &&
          referral.crn.contains(caseReferenceNumberOrPersonName, ignoreCase = true)
        val hasOtherFilters = hasFiltersOtherThanSearch(cohorts, sexes, probationDeliveryUnits, reportingTeams)
        val shouldSkipExclusionFilter = crnMatchesSearch && !hasOtherFilters

        if (!shouldSkipExclusionFilter) {
          val access = limitedAccessOffenderAccessMap?.get(referral.crn)
          val isLimitedAccessOffender = access?.isLimitedAccessOffender ?: false
          val isExcluded = access?.isExcluded ?: false
          if (isLimitedAccessOffender) {
            return@filter !isExcluded
          }
        }
      }

      return@filter true
    }.map { referral ->
      val access = limitedAccessOffenderAccessMap?.get(referral.crn)
      if (exclusionAccessCheckEnabled) {
        referral.toApi(
          isLimitedAccessOffender = access?.isLimitedAccessOffender ?: false,
          isExcluded = access?.isExcluded ?: false,
        )
      } else {
        referral.toApi(isLimitedAccessOffender = access?.isLimitedAccessOffender ?: false)
      }
    }

    val referralsToReturn = PageImpl(
      if (exclusionAccessCheckEnabled) {
        referralCaseListItems.sortedBy { it.isExcluded }
      } else {
        referralCaseListItems
      },
      referralsPage.pageable,
      referralsPage.totalElements - (referralsPage.content.size - referralCaseListItems.size).toLong(),
    )

    val otherTabCount = getReferralCaseList(
      pageable = pageable,
      openOrClosed = if (openOrClosed == OpenOrClosed.OPEN) OpenOrClosed.CLOSED else OpenOrClosed.OPEN,
      username = username,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      offenceCohortAndLdcPairs = offenceCohortAndLdcPairs,
      statuses = normalisedStatuses,
      sexes = sexes,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    ).totalElements

    return CaseListReferrals(referralsToReturn, otherTabCount.toInt(), this.getCaseListFilterData(userRegionNames))
  }

  private fun getReferralCaseList(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    username: String,
    caseReferenceNumberOrPersonName: String?,
    offenceCohortAndLdcPairs: List<Pair<OffenceCohort, Boolean>>,
    statuses: List<String>?,
    sexes: List<String>?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): Page<ReferralCaseListItemViewEntity> {
    val possibleStatuses = referralStatusService.getOpenOrClosedStatusesDescriptions(openOrClosed)

    val baseSpec =
      getReferralCaseListItemSpecification(
        possibleStatuses = possibleStatuses,
        crnOrPersonName = caseReferenceNumberOrPersonName,
        offenceCohortAndLdcPairs = offenceCohortAndLdcPairs,
        statuses = statuses,
        sexes = sexes,
        pdus = probationDeliveryUnits,
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

    val allowedCRNsForUser = if (!exclusionAccessCheckEnabled) {
      crns
        .chunked(500)
        .flatMap { userService.getAccessibleOffenders(username, it) }
        .toSet()
    } else {
      crns.toSet()
    }

    if (allowedCRNsForUser.isEmpty()) {
      log.warn("No CRNs found for user: $username. Returning empty list for ReferralCaseList.")
      return PageImpl(emptyList(), pageable, 0)
    }

    val restrictedSpec = withCrns(specWithRegions, allowedCRNsForUser)
    val totalAllowedCount = referralCaseListItemRepository.count(restrictedSpec)
    val caseListReferrals = referralCaseListItemRepository.findAll(restrictedSpec, pageable)

    if (caseListReferrals.totalElements < 50) log.warn("Only ${caseListReferrals.totalElements} out of ${pageable.pageSize} referrals returned due to Limited Access Offender check ")
    return PageImpl(caseListReferrals.content, pageable, totalAllowedCount)
  }

  private fun isFilterApplied(
    caseReferenceNumberOrPersonName: String?,
    cohorts: List<ProgrammeGroupCohort>?,
    sexes: List<String>?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): Boolean = !caseReferenceNumberOrPersonName.isNullOrEmpty() ||
    !cohorts.isNullOrEmpty() ||
    !sexes.isNullOrEmpty() ||
    probationDeliveryUnits != null ||
    reportingTeams != null

  private fun hasFiltersOtherThanSearch(
    cohorts: List<ProgrammeGroupCohort>?,
    sexes: List<String>?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): Boolean = !cohorts.isNullOrEmpty() ||
    !sexes.isNullOrEmpty() ||
    probationDeliveryUnits != null ||
    reportingTeams != null

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
    val openDescriptions =
      ReferralStatusUtils.sortStatuses(open.map { ReferralStatusUtils.formatStatus(it.description) })

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
