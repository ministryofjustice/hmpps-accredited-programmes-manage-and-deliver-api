package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getGroupWaitlistItemSpecification
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val userService: UserService,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun createGroup(createGroupRequest: CreateGroupRequest, username: String): ProgrammeGroupEntity? {
    programmeGroupRepository.findByCode(createGroupRequest.groupCode)
      ?.let { throw ConflictException("Programme group with code ${createGroupRequest.groupCode} already exists") }

    val (userRegion) = userService.getUserRegions(username)

    log.info("Group created with code: ${createGroupRequest.groupCode}")

    return programmeGroupRepository.save(createGroupRequest.toEntity(userRegion))
  }

  fun getGroupWaitlistDataByCriteria(
    pageable: Pageable,
    selectedTab: GroupPageTab,
    groupId: UUID,
    sex: String?,
    cohort: ProgrammeGroupCohort?,
    nameOrCRN: String?,
    pdu: String?,
    reportingTeams: List<String>?,
  ): ProgrammeGroupDetails {
    // Verify the group exists first
    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Programme group with id $groupId not found")

    val otherTab = if (selectedTab === GroupPageTab.WAITLIST) GroupPageTab.ALLOCATED else GroupPageTab.WAITLIST

    val activeSpecification =
      getGroupWaitlistItemSpecification(selectedTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

    val nonActiveSpecification =
      getGroupWaitlistItemSpecification(otherTab, groupId, sex, cohort, nameOrCRN, pdu, reportingTeams)

    val grouplistDataToReturn: Page<GroupItem> =
      groupWaitlistItemViewRepository.findAll(activeSpecification, pageable).map { it.toApi() }

    val otherTabCount: Int = groupWaitlistItemViewRepository.count(nonActiveSpecification).toInt()

    return ProgrammeGroupDetails(
      group = ProgrammeGroupDetails.Group(
        code = group.code,
        regionName = group.regionName,
      ),
      filters = getGroupFilters(),
      pagedGroupData = grouplistDataToReturn,
      otherTabTotal = otherTabCount,
    )
  }

  fun getGroupFilters(): ProgrammeGroupDetails.Filters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()

    return ProgrammeGroupDetails.Filters(
      pduNames = referralReportingLocations.map { it.pduName }.distinct(),
      reportingTeams = referralReportingLocations.map { it.reportingTeam }.distinct(),
    )
  }
}
