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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupsByRegion
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupDetails
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroupModule
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroupSession
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.StartDateText
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.toFacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getGroupWaitlistItemSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getProgrammeGroupsSpecification
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.compareTo
import kotlin.toString

@Service
@Transactional
class ProgrammeGroupService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val groupWaitlistItemViewRepository: GroupWaitlistItemViewRepository,
  private val referralReportingLocationRepository: ReferralReportingLocationRepository,
  private val userService: UserService,
  private val accreditedProgrammeTemplateRepository: AccreditedProgrammeTemplateRepository,
  private val scheduleService: ScheduleService,
  private val sessionRepository: SessionRepository,
  private val facilitatorService: FacilitatorService,
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
    programmeGroup.treatmentManager = facilitatorService.findOrCreateFacilitator(treatmentManagers.first())

    facilitators.forEach { facilitatorRequest ->
      val facilitatorEntity = facilitatorService.findOrCreateFacilitator(facilitatorRequest)

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

  fun getModuleSessionsForGroup(groupId: UUID): ProgrammeGroupModuleSessionsResponse? {
    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val programmeGroupModuleSessionsResponseGroup = ProgrammeGroupModuleSessionsResponseGroup(group.code, group.regionName)

    val modules = group.accreditedProgrammeTemplate?.modules?.map { module ->
      val sessions = module.sessionTemplates.map { sessionTemplate ->
        val scheduledSessions = getScheduledSessionForGroupAndSessionTemplate(
          groupId = group.id!!,
          sessionTemplateId = sessionTemplate.id!!,
        )
        scheduledSessions?.map { scheduledSession ->
          ProgrammeGroupModuleSessionsResponseGroupSession(
            id = sessionTemplate.id!!,
            number = sessionTemplate.sessionNumber,
            name = sessionTemplate.name,
            type = sessionTemplate.sessionType,
            dateOfSession = scheduledSession.startsAt.toLocalDate().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy")).toString(),
            timeOfSession = formatTimeForUiDisplay(scheduledSession.startsAt.toLocalTime()),
            participants = if (sessionTemplate.sessionType == SessionType.GROUP) listOf("All") else listOf("The person"),
            facilitators = group.groupFacilitators.map { it.facilitator.personName },
          )
        } ?: emptyList()
      }.flatten()

      ProgrammeGroupModuleSessionsResponseGroupModule(
        id = module.id!!,
        number = module.moduleNumber,
        name = module.name,
        startDateText = StartDateText(
          "Estimated start date of ${module.name} one to ones",
          group.sessions
            .filter { it.moduleSessionTemplate.sessionType == SessionType.ONE_TO_ONE }
            .minByOrNull { it.startsAt }?.startsAt?.toLocalDate()
            ?.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"))
            .toString(),
        ),
        scheduleButtonText = "Schedule a ${module.name} session",
        sessions = sessions,
      )
    }.orEmpty()

    return ProgrammeGroupModuleSessionsResponse(programmeGroupModuleSessionsResponseGroup, modules)
  }

  private fun formatTimeForUiDisplay(time: LocalTime): String = when {
    time.hour == 12 && time.minute == 0 -> "midday"
    time.hour == 0 -> "midnight"
    time.hour == 0 -> "12:${time.minute}am"
    time.hour < 12 -> "${time.hour}:${time.minute}am"
    time.hour == 12 -> "12:${time.minute}pm"
    else -> "${time.hour - 12}:${time.minute}pm"
  }

  fun getScheduledSessionForGroupAndSessionTemplate(
    groupId: UUID,
    sessionTemplateId: UUID,
  ): List<SessionEntity>? = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(sessionTemplateId, groupId)

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
