package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.LocationFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupTeamMember
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupSchedule
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupScheduleSession
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupsByRegion
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.FacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.toFacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getGroupWaitlistItemSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getProgrammeGroupsSpecification
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val userService: UserService,
  private val facilitatorRepository: FacilitatorRepository,
  private val accreditedProgrammeTemplateRepository: AccreditedProgrammeTemplateRepository,
  private val scheduleService: ScheduleService,
  private val sessionRepository: SessionRepository,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun createGroup(createGroupRequest: CreateGroupRequest, username: String): ProgrammeGroupEntity {
    val (userRegion) = userService.getUserRegions(username)
    programmeGroupRepository.findByCodeAndRegionName(createGroupRequest.groupCode, userRegion.description)
      ?.let { throw ConflictException("Programme group with code ${createGroupRequest.groupCode} already exists in region") }

    val programmeGroup = createGroupRequest.toEntity(userRegion.description)

    val (treatmentManagers, facilitators) = createGroupRequest.teamMembers.partition {
      it.teamMemberType == CreateGroupTeamMemberType.TREATMENT_MANAGER
    }

    require(treatmentManagers.isNotEmpty()) {
      "At least one treatment manager must be specified for a programme group"
    }

    require(treatmentManagers.size == 1) {
      "Exactly one treatment manager must be specified for a programme group"
    }
    programmeGroup.treatmentManager = findOrCreateFacilitator(treatmentManagers.first())

    facilitators.forEach { facilitatorRequest ->
      val facilitatorEntity = findOrCreateFacilitator(facilitatorRequest)

      val groupFacilitator = ProgrammeGroupFacilitatorEntity(
        facilitator = facilitatorEntity,
        facilitatorType = facilitatorRequest.teamMemberType.toFacilitatorType(),
        programmeGroup = programmeGroup,
      )

      programmeGroup.groupFacilitators.add(groupFacilitator)
    }

    val slots = createSessionSlots(createGroupRequest.createGroupSessionSlot, programmeGroup)
    programmeGroup.programmeGroupSessionSlots.addAll(slots)

    val buildingChoicesTemplate = accreditedProgrammeTemplateRepository.findFirstByName("Building Choices")
    programmeGroup.accreditedProgrammeTemplate = buildingChoicesTemplate
    require(buildingChoicesTemplate != null) { "Template must not be null" }

    log.info("Group created with code: ${createGroupRequest.groupCode}")

    val savedGroup = programmeGroupRepository.save(programmeGroup)

    scheduleService.scheduleSessionsForGroup(savedGroup.id!!)

    return savedGroup
  }

  private fun findOrCreateFacilitator(teamMember: CreateGroupTeamMember): FacilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(teamMember.facilitatorCode)
    ?: facilitatorRepository.save(teamMember.toFacilitatorEntity())

  private fun createSessionSlots(
    slotData: Set<CreateGroupSessionSlot>,
    programmeGroup: ProgrammeGroupEntity,
  ): List<ProgrammeGroupSessionSlotEntity> = slotData.map { item ->
    val hour = when (item.amOrPm) {
      AmOrPm.PM if item.hour < 12 -> item.hour + 12
      AmOrPm.AM if item.hour == 12 -> 0
      else -> item.hour
    }
    val startTime = LocalTime.of(hour, item.minutes)
    ProgrammeGroupSessionSlotEntity(
      programmeGroup = programmeGroup,
      dayOfWeek = item.dayOfWeek,
      startTime = startTime,
    )
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

    val groupListDataToReturn: Page<GroupItem> =
      groupWaitlistItemViewRepository.findAll(activeSpecification, pageable).map { it.toApi() }

    val otherTabCount: Int = groupWaitlistItemViewRepository.count(nonActiveSpecification).toInt()

    return ProgrammeGroupDetails(
      group = Group(
        id = group.id,
        code = group.code,
        regionName = group.regionName,
      ),
      filters = getGroupFilters(),
      pagedGroupData = groupListDataToReturn,
      otherTabTotal = otherTabCount,
    )
  }

  fun getGroupInRegion(groupCode: String, username: String): ProgrammeGroupEntity? {
    val (userRegion) = userService.getUserRegions(username)
    return programmeGroupRepository.findByCodeAndRegionName(groupCode, userRegion.description)
  }

  fun getGroupFilters(): ProgrammeGroupDetails.Filters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val pdusWithReportingTeams = referralReportingLocations.groupBy { it.pduName }
      .map { (pduName, reportingTeams) ->
        LocationFilterValues(pduName = pduName, reportingTeams = reportingTeams.map { it.reportingTeam }.distinct())
      }

    return ProgrammeGroupDetails.Filters(
      locationFilterValues = pdusWithReportingTeams,
    )
  }

  fun getScheduleForGroup(groupId: UUID): GroupSchedule? {
    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val sessions = sessionRepository.findByProgrammeGroupId(groupId)

    val scheduleSessions = sessions.map { session ->
      GroupScheduleSession(
        id = session.id,
        name = session.moduleSessionTemplate.name,
        // TODO: ModuleSessionTemplateEntity.sessionType instead for type?
        type = if (session.moduleSessionTemplate.name.contains("one-to-one", ignoreCase = true)) "Individual" else "Group",
        date = session.startsAt.toLocalDate().toString(),
        time = formatTimeForUiDisplay(session.startsAt.toLocalTime()),
      )
    }

    val preGroupOneToOneDate = sessions
      .filter { it.moduleSessionTemplate.name == "Pre-group one-to-ones" }
      .minByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()?.toString() ?: ""

    val gettingStartedStartDate = sessions
      .filter { it.moduleSessionTemplate.name.startsWith("Getting started") }
      .minByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()?.toString() ?: ""

    val endDate = sessions
      .maxByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()?.toString() ?: ""

    return GroupSchedule(
      preGroupOneToOneStartDate = preGroupOneToOneDate,
      gettingStartedModuleStartDate = gettingStartedStartDate,
      endDate = endDate,
      modules = scheduleSessions,
    )
  }

  private fun formatTimeForUiDisplay(time: LocalTime): String = when {
    time.hour == 12 && time.minute == 0 -> "midday"
    time.hour == 0 -> "midnight"
    time.hour == 0 -> "12:${time.minute}am"
    time.hour < 12 -> "${time.hour}:${time.minute}am"
    time.hour == 12 -> "12:${time.minute}pm"
    else -> "${time.hour - 12}:${time.minute}pm"
  }

  fun getProgrammeGroupsForRegion(
    pageable: Pageable,
    groupCode: String?,
    pdu: String?,
    deliveryLocations: List<String>?,
    cohort: String?,
    sex: String?,
    selectedTab: GroupPageByRegionTab,
    username: String,
  ): GroupsByRegion {
    val groupCohort = if (cohort.isNullOrEmpty()) null else ProgrammeGroupCohort.fromString(cohort)

    val distinctRegionDescriptions = userService.getUserRegions(username).map { it.description }.distinct()

    if (distinctRegionDescriptions.isEmpty()) {
      throw NotFoundException("Cannot find any regions (or teams) for user $username")
    } else if (distinctRegionDescriptions.size > 1) {
      log.warn("User $username has more than one region on their account, going to use '${distinctRegionDescriptions.first()}'")
    }

    val firstUserRegionDescription = distinctRegionDescriptions.first()

    // Base spec without startedAt filter (used for total count)
    val baseSpec = getProgrammeGroupsSpecification(
      groupCode = groupCode,
      pdu = pdu,
      deliveryLocations = deliveryLocations,
      cohort = groupCohort,
      sex = sex,
      regionName = firstUserRegionDescription,
    )

    // Apply tab-specific date filter
    val startedAtSpec = Specification<ProgrammeGroupEntity> { root, _, cb ->
      val datePath = root.get<LocalDate>("earliestPossibleStartDate")
      when (selectedTab) {
        GroupPageByRegionTab.NOT_STARTED -> cb.or(
          cb.isNull(datePath),
          cb.greaterThan(datePath, LocalDate.now()),
        )

        GroupPageByRegionTab.IN_PROGRESS_OR_COMPLETE -> cb.lessThanOrEqualTo(datePath, LocalDate.now())
      }
    }

    val activeSpec = baseSpec.and(startedAtSpec)

    val pagedData: Page<Group> = programmeGroupRepository.findAll(activeSpec, pageable).map { it.toApi() }

    val totalForAllTabs: Long = programmeGroupRepository.count(baseSpec)
    val otherTabTotal: Int = (totalForAllTabs - pagedData.totalElements).toInt()

    val allPduNames = programmeGroupRepository.findDistinctProbationDeliveryUnitNames(firstUserRegionDescription)

    val deliveryLocationNames = if (pdu.isNullOrEmpty()) {
      null
    } else {
      programmeGroupRepository.findDistinctDeliveryLocationNames(firstUserRegionDescription, pdu)
    }

    return GroupsByRegion(
      pagedGroupData = pagedData,
      otherTabTotal = otherTabTotal,
      regionName = firstUserRegionDescription,
      probationDeliveryUnitNames = allPduNames,
      deliveryLocationNames = deliveryLocationNames,
    )
  }
}
