package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
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
  private companion object {
    val STATUS_SORT_PROPERTIES = setOf("status", "referralStatus")
  }

  private val log = LoggerFactory.getLogger(this::class.java)
  fun getReferralCaseListItemServiceByCriteria(
    pageable: Pageable,
    openOrClosed: OpenOrClosed,
    username: String,
    caseReferenceNumberOrPersonName: String?,
    cohort: ProgrammeGroupCohort?,
    status: String?,
    sex: String?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): CaseListReferrals {
    val (offenceType, hasLdc) = cohort?.let { ProgrammeGroupCohort.toOffenceTypeAndLdc(it) }
      ?: (null to null)

    val isFilteredCaseList =
      isFilterApplied(caseReferenceNumberOrPersonName, cohort, sex, probationDeliveryUnits, reportingTeams)
    val userRegionNames = userService.getUserRegionNames(username)

    // Normalise the status filter once so both the main query and the otherTabCount query
    // receive the same DB-compatible value (e.g. "Breach" -> "Breach (non-attendance)").
    val normalisedStatus = ReferralStatusUtils.unformatStatus(status)

    val caseListQuery = buildCaseListQuery(
      openOrClosed = openOrClosed,
      username = username,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      offenceCohort = offenceType,
      hasLdc = hasLdc,
      status = normalisedStatus,
      sex = sex,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    )

    // The full result set is sorted by the database, then excluded referrals are lifted out of that
    // ordering and appended at the end, so that pagination is applied to the final ordering.
    val statusOrder = pageable.sort.find { it.property in STATUS_SORT_PROPERTIES }
    val databaseSort = Sort.by(pageable.sort.filterNot { it.property in STATUS_SORT_PROPERTIES })

    val queriedReferrals = caseListQuery.specification
      ?.let { referralCaseListItemRepository.findAll(it, databaseSort) }
      ?: emptyList()

    // Statuses are ordered by their position in the referral workflow rather than alphabetically
    val sortedReferrals = statusOrder
      ?.let { order ->
        val byStatus = compareBy<ReferralCaseListItemViewEntity> { ReferralStatusUtils.statusSortIndex(it.status) }
        queriedReferrals.sortedWith(if (order.isDescending) byStatus.reversed() else byStatus)
      }
      ?: queriedReferrals

    val (excludedReferrals, includedReferrals) = if (exclusionAccessCheckEnabled) {
      sortedReferrals.partition { it.crn in caseListQuery.excludedCrns }
    } else {
      emptyList<ReferralCaseListItemViewEntity>() to sortedReferrals
    }

    val orderedReferrals = includedReferrals + retainedExcludedReferrals(
      excludedReferrals = excludedReferrals,
      isFilteredCaseList = isFilteredCaseList,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      cohort = cohort,
      sex = sex,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    )

    val pageContent = if (pageable.isPaged) {
      orderedReferrals.drop(pageable.offset.toInt()).take(pageable.pageSize)
    } else {
      orderedReferrals
    }

    // Fetch Limited Access Offender (LAO) status for all distinct case reference numbers (CRNs)
    var limitedAccessOffenderAccessMap: Map<String, Access>? = null
    if (limitedAccessOffenderCheckEnabled) {
      val caseReferenceNumbers = pageContent.map { it.crn }.distinct()
      limitedAccessOffenderAccessMap = userAccessService.determineUserAccess(username, caseReferenceNumbers)
    }

    val referralCaseListItems = pageContent.map { referral ->
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

    val referralsToReturn = PageImpl(referralCaseListItems, pageable, orderedReferrals.size.toLong())

    val otherTabCount = buildCaseListQuery(
      openOrClosed = if (openOrClosed == OpenOrClosed.OPEN) OpenOrClosed.CLOSED else OpenOrClosed.OPEN,
      username = username,
      caseReferenceNumberOrPersonName = caseReferenceNumberOrPersonName,
      offenceCohort = offenceType,
      hasLdc = hasLdc,
      status = normalisedStatus,
      sex = sex,
      probationDeliveryUnits = probationDeliveryUnits,
      reportingTeams = reportingTeams,
    ).specification?.let { referralCaseListItemRepository.count(it) } ?: 0L

    return CaseListReferrals(referralsToReturn, otherTabCount.toInt(), this.getCaseListFilterData(userRegionNames))
  }

  // Excluded referrals for a limited access offender are hidden entirely once the user has applied a filter,
  // unless the only filter is a search that matches their CRN.
  private fun retainedExcludedReferrals(
    excludedReferrals: List<ReferralCaseListItemViewEntity>,
    isFilteredCaseList: Boolean,
    caseReferenceNumberOrPersonName: String?,
    cohort: ProgrammeGroupCohort?,
    sex: String?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): List<ReferralCaseListItemViewEntity> {
    if (!isFilteredCaseList || !limitedAccessOffenderCheckEnabled || excludedReferrals.isEmpty()) return excludedReferrals

    val hasOtherFilters = hasFiltersOtherThanSearch(cohort, sex, probationDeliveryUnits, reportingTeams)

    return excludedReferrals.filterNot { referral ->
      val crnMatchesSearch = !caseReferenceNumberOrPersonName.isNullOrEmpty() &&
        referral.crn.contains(caseReferenceNumberOrPersonName, ignoreCase = true)

      !(crnMatchesSearch && !hasOtherFilters) && userAccessService.isLimitedAccessOffender(referral.crn)
    }
  }

  private data class CaseListQuery(
    val specification: Specification<ReferralCaseListItemViewEntity>?,
    val excludedCrns: Set<String>,
  )

  private fun buildCaseListQuery(
    openOrClosed: OpenOrClosed,
    username: String,
    caseReferenceNumberOrPersonName: String?,
    offenceCohort: OffenceCohort?,
    hasLdc: Boolean?,
    status: String?,
    sex: String?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): CaseListQuery {
    val possibleStatuses = referralStatusService.getOpenOrClosedStatusesDescriptions(openOrClosed)

    val baseSpec =
      getReferralCaseListItemSpecification(
        possibleStatuses = possibleStatuses,
        crnOrPersonName = caseReferenceNumberOrPersonName,
        offenceCohort = offenceCohort,
        hasLdc = hasLdc,
        status = status,
        sex = sex,
        pdus = probationDeliveryUnits,
        reportingTeams = reportingTeams,
      )

    val userRegions = userService.getUserRegionNames(username)
    if (userRegions.isEmpty()) {
      log.warn("No regions found for user: $username. Returning empty list for ReferralCaseList.")
      return CaseListQuery(null, emptySet())
    }
    val specWithRegions = withRegionNames(baseSpec, userRegions)
    val crns = referralCaseListItemRepository.findAllCrns(specWithRegions).distinct()

    val accessibleCRNsForUser = crns
      .chunked(500)
      .flatMap { userService.getAccessibleOffenders(username, it) }
      .toSet()

    // When the exclusion check is enabled, excluded referrals are kept in the result set and
    // re-ordered later rather than being filtered out by the query.
    val allowedCRNsForUser = if (exclusionAccessCheckEnabled) crns.toSet() else accessibleCRNsForUser
    val excludedCrns = if (exclusionAccessCheckEnabled) crns.toSet() - accessibleCRNsForUser else emptySet()

    if (allowedCRNsForUser.isEmpty()) {
      log.warn("No CRNs found for user: $username. Returning empty list for ReferralCaseList.")
      return CaseListQuery(null, emptySet())
    }

    return CaseListQuery(withCrns(specWithRegions, allowedCRNsForUser), excludedCrns)
  }

  private fun isFilterApplied(
    caseReferenceNumberOrPersonName: String?,
    cohort: ProgrammeGroupCohort?,
    sex: String?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): Boolean = !caseReferenceNumberOrPersonName.isNullOrEmpty() ||
    cohort != null ||
    !sex.isNullOrEmpty() ||
    probationDeliveryUnits != null ||
    reportingTeams != null

  private fun hasFiltersOtherThanSearch(
    cohort: ProgrammeGroupCohort?,
    sex: String?,
    probationDeliveryUnits: List<String>?,
    reportingTeams: List<String>?,
  ): Boolean = cohort != null ||
    !sex.isNullOrEmpty() ||
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
