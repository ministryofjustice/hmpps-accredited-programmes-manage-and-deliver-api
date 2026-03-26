package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import jakarta.persistence.criteria.Subquery
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.LocationFilterValues
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AttendanceAndSessionNotes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.Group
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupDetailsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupItem
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupScheduleOverview
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupScheduleOverviewSession
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupSessionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.GroupsByRegion
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupAllocations
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupCohort
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroup
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroupModule
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ProgrammeGroupModuleSessionsResponseGroupSession
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.StartDateText
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.UpdateGroupResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toApi
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.CreateGroupTeamMemberType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageByRegionTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.type.GroupPageTab
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.ConflictException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupMembershipEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusDescriptionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.FacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.UAAB
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.toFacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.AccreditedProgrammeTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.GroupWaitlistItemViewRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralReportingLocationRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getGroupWaitlistItemSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.specification.getProgrammeGroupsSpecification
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.formatTimeForUiDisplay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

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
  private val sessionNameFormatter: SessionNameFormatter,
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

  fun updateGroup(updateGroupRequest: UpdateGroupRequest, groupId: UUID, username: String): UpdateGroupResponse {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Programme group with id $groupId not found")

    // Track what was updated for the success message
    var updatedField: String? = null

    updateGroupRequest.groupCode?.let {
      programmeGroup.code = it
      updatedField = "groupCode"
    }
    updateGroupRequest.sex?.let {
      programmeGroup.sex = it
      updatedField = "sex"
    }
    updateGroupRequest.earliestStartDate?.let {
      programmeGroup.earliestPossibleStartDate = it
      updatedField = "earliestStartDate"
    }
    updateGroupRequest.pduName?.let {
      programmeGroup.probationDeliveryUnitName = it
      updatedField = "deliveryLocation"
    }
    updateGroupRequest.pduCode?.let {
      programmeGroup.probationDeliveryUnitCode = it
      updatedField = "deliveryLocation"
    }
    updateGroupRequest.deliveryLocationName?.let {
      programmeGroup.deliveryLocationName = it
      updatedField = "deliveryLocation"
    }
    updateGroupRequest.deliveryLocationCode?.let {
      programmeGroup.deliveryLocationCode = it
      updatedField = "deliveryLocation"
    }

    // Update cohort and isLdc if cohort is provided
    updateGroupRequest.cohort?.let { cohort ->
      val (offenceType, isLdc) = ProgrammeGroupCohort.toOffenceTypeAndLdc(cohort)
      programmeGroup.cohort = offenceType
      programmeGroup.isLdc = isLdc
      updatedField = "cohort"
    }

    // Update session slots if provided
    updateGroupRequest.createGroupSessionSlot?.let { sessionSlots ->
      programmeGroup.programmeGroupSessionSlots.clear()
      val slots = createSessionSlots(sessionSlots, programmeGroup)
      programmeGroup.programmeGroupSessionSlots.addAll(slots)
      updatedField = "daysAndTimes"
    }

    // Update team members if provided
    updateGroupRequest.teamMembers?.let { teamMembers ->
      if (teamMembers.isNotEmpty()) {
        val (treatmentManagers, facilitators) = teamMembers.partition {
          it.teamMemberType == CreateGroupTeamMemberType.TREATMENT_MANAGER
        }

        // Update treatment manager if provided
        if (treatmentManagers.isNotEmpty()) {
          require(treatmentManagers.size == 1) {
            "Exactly one treatment manager must be specified for a programme group"
          }
          programmeGroup.treatmentManager = facilitatorService.findOrCreateFacilitator(treatmentManagers.first())
        }

        // Update facilitators - clear existing and add new ones
        programmeGroup.groupFacilitators.clear()
        facilitators.forEach { facilitatorRequest ->
          val facilitatorEntity = facilitatorService.findOrCreateFacilitator(facilitatorRequest)

          val groupFacilitator = ProgrammeGroupFacilitatorEntity(
            facilitator = facilitatorEntity,
            facilitatorType = facilitatorRequest.teamMemberType.toFacilitatorType(),
            programmeGroup = programmeGroup,
          )

          programmeGroup.groupFacilitators.add(groupFacilitator)
        }
        updatedField = "teamMembers"
      }
    }

    programmeGroup.updatedAt = LocalDateTime.now()
    programmeGroup.updatedByUsername = username

    val savedGroup = programmeGroupRepository.save(programmeGroup)

    // Reschedule future sessions if requested
    if (updateGroupRequest.automaticallyRescheduleOtherSessions == true) {
      scheduleService.rescheduleSessionsForGroup(savedGroup.id!!)
    }

    val successMessage = getUpdateSuccessMessage(updatedField)
    return UpdateGroupResponse(successMessage = successMessage)
  }

  private fun getUpdateSuccessMessage(updatedField: String?): String = when (updatedField) {
    "groupCode" -> "The group code has been updated."
    "earliestStartDate" -> "The start date has been updated."
    "daysAndTimes" -> "The days and times have been updated."
    "cohort" -> "The cohort has been updated."
    "sex" -> "The gender has been updated."
    "deliveryLocation" -> "The delivery location has been updated."
    "teamMembers" -> "The people responsible for the group have been updated."
    else -> "The group has been updated."
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
  ): ProgrammeGroupAllocations {
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

    return ProgrammeGroupAllocations(
      group = Group(
        id = group.id,
        code = group.code,
        regionName = group.regionName,
      ),
      filters = getGroupAllocationsFilters(),
      pagedGroupData = groupListDataToReturn,
      otherTabTotal = otherTabCount,
    )
  }

  fun getGroupInRegion(groupCode: String, username: String): ProgrammeGroupEntity? {
    val (userRegion) = userService.getUserRegions(username)
    return programmeGroupRepository.findByCodeAndRegionName(groupCode, userRegion.description)
  }

  fun getGroupDetails(groupId: UUID): GroupDetailsResponse {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")
    val daysAndTimes: List<String> = programmeGroup.programmeGroupSessionSlots.map { "${it.dayOfWeek.toAvailabilityOptions()}, ${formatTimeOfSession(it.startTime, it.startTime.plusMinutes(150))}" }
    return GroupDetailsResponse.from(programmeGroup, daysAndTimes)
  }

  fun getGroupAllocationsFilters(): ProgrammeGroupAllocations.ProgrammeGroupAllocationsFilters {
    val referralReportingLocations = referralReportingLocationRepository.getPdusAndReportingTeams()
    val pdusWithReportingTeams = referralReportingLocations.groupBy { it.pduName }
      .map { (pduName, reportingTeams) ->
        LocationFilterValues(pduName = pduName, reportingTeams = reportingTeams.map { it.reportingTeam }.distinct())
      }

    return ProgrammeGroupAllocations.ProgrammeGroupAllocationsFilters(
      locationFilterValues = pdusWithReportingTeams,
    )
  }

  fun getModuleSessionsForGroup(groupId: UUID): ProgrammeGroupModuleSessionsResponse? {
    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val programmeGroupModuleSessionsResponseGroup =
      ProgrammeGroupModuleSessionsResponseGroup(group.code, group.regionName)

    // We need to get all modules for the group's accredited programme template. From there, we get all the session templates for each module,
    // and then we need to go to the database to find any scheduled sessions for the group that use that session template.
    // We need to do this as we currently have no direct link from session templates to scheduled sessions.
    // This then builds the api response object with all the required data.
    val modules = group.accreditedProgrammeTemplate?.modules?.map { module ->
      val sessions = module.sessionTemplates.flatMap { sessionTemplate ->
        val scheduledSessions = getScheduledSessionForGroupAndSessionTemplate(
          groupId = group.id!!,
          sessionTemplateId = sessionTemplate.id!!,
        )?.filter { !it.isPlaceholder }
        scheduledSessions?.map { scheduledSession ->
          ProgrammeGroupModuleSessionsResponseGroupSession(
            id = scheduledSession.id!!,
            number = sessionTemplate.sessionNumber,
            name = sessionNameFormatter.format(
              scheduledSession,
              SessionNameContext.SessionsAndAttendance(sessionTemplate),
            ),
            type = if (sessionTemplate.sessionType == SessionType.ONE_TO_ONE) "Individual" else "Group",
            dateOfSession = scheduledSession.startsAt.toLocalDate(),
            timeOfSession = formatTimeOfSession(
              scheduledSession.startsAt.toLocalTime(),
              scheduledSession.endsAt.toLocalTime(),
            ),
            participants = if (sessionTemplate.sessionType == SessionType.GROUP) listOf("All") else scheduledSession.attendees.map { it.personName },
            facilitators = scheduledSession.sessionFacilitators.filter { it.facilitatorType != FacilitatorType.COVER_FACILITATOR }
              .map { it.facilitator.personName },
          )
        } ?: emptyList()
      }

      ProgrammeGroupModuleSessionsResponseGroupModule(
        id = module.id!!,
        number = module.moduleNumber,
        name = module.name,
        startDateText = StartDateText(
          formatEstimatedStartText(module.name),
          group.sessions
            .filter { it.moduleSessionTemplate.sessionType == SessionType.ONE_TO_ONE }
            .minByOrNull { it.startsAt }?.startsAt?.toLocalDate()
            ?.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy"))
            .toString(),
        ),
        scheduleButtonText = formatButtonText(module.name),
        sessions = sessions,
      )
    }.orEmpty()

    return ProgrammeGroupModuleSessionsResponse(programmeGroupModuleSessionsResponseGroup, modules)
  }

  private fun formatButtonText(moduleName: String): String = when (moduleName) {
    "Pre-group one-to-ones" -> "Schedule a pre-group session"
    "Post-programme reviews" -> "Schedule a post-programme review"
    else -> "Schedule a $moduleName session"
  }

  private fun formatEstimatedStartText(moduleName: String): String = when (moduleName) {
    "Pre-group one-to-ones" -> "Estimated start date of ${moduleName.lowercase()}"
    "Post-programme reviews" -> "Post-programme reviews deadline"
    else -> "Estimated date of $moduleName one-to-ones"
  }

  private fun formatTimeOfSession(startTime: LocalTime, endTime: LocalTime): String {
    val formattedStartTime = formatTimeForUiDisplay(startTime)
    val formattedEndTime = formatTimeForUiDisplay(endTime)
    return "$formattedStartTime to $formattedEndTime"
  }

  fun getScheduledSessionForGroupAndSessionTemplate(
    groupId: UUID,
    sessionTemplateId: UUID,
  ): List<SessionEntity>? = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(sessionTemplateId, groupId)

  fun getScheduleOverviewForGroup(groupId: UUID): GroupScheduleOverview {
    val group = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val sessions = sessionRepository.findByProgrammeGroupId(groupId)

    if (sessions.isEmpty()) {
      throw NotFoundException("No sessions found for group $groupId")
    }

    val groupSessions =
      sessions.filter { it.sessionType == SessionType.GROUP || (it.sessionType == SessionType.ONE_TO_ONE && it.isPlaceholder) }
        .map { session ->
          val sessionName = sessionNameFormatter.format(session, SessionNameContext.ScheduleOverview)
          GroupScheduleOverviewSession(
            id = session.id,
            name = sessionName,
            type = session.sessionType.value,
            date = session.startsAt.toLocalDate(),
            time = if (session.isPlaceholder) {
              "Various times"
            } else {
              "${formatTimeForUiDisplay(session.startsAt.toLocalTime())} to ${
                formatTimeForUiDisplay(session.endsAt.toLocalTime())
              }"
            },
          )
        }

    val preGroupOneToOneDate = sessions
      .filter { it.moduleSessionTemplate.module.name.startsWith("Pre-group", ignoreCase = true) }
      .minByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()

    val gettingStartedStartDate = sessions
      .filter { it.moduleSessionTemplate.module.name.startsWith("Getting started", ignoreCase = true) }
      .minByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()

    val endDate = sessions
      .maxByOrNull { it.startsAt }
      ?.startsAt?.toLocalDate()

    return GroupScheduleOverview(
      preGroupOneToOneStartDate = preGroupOneToOneDate,
      gettingStartedModuleStartDate = gettingStartedStartDate,
      endDate = endDate,
      sessions = groupSessions,
      groupCode = group.code,
    )
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
    val startedAtSpec = Specification<ProgrammeGroupEntity> { root, query, cb ->
      val datePath = root.get<LocalDate>("earliestPossibleStartDate")

      // Subquery to find referrals that DO NOT have "Programme complete" status in their history
      val subquery: Subquery<Long> = query.subquery(Long::class.java)
      val membershipRoot = subquery.from(ProgrammeGroupMembershipEntity::class.java)
      val referralJoin = membershipRoot.join<ProgrammeGroupMembershipEntity, ReferralEntity>("referral")

      // We want to count memberships in this group where the referral DOES NOT have a "Programme complete" status
      // To do this, we can check if there's no "Programme complete" status history for that referral.
      val historySubquery: Subquery<Long> = subquery.subquery(Long::class.java)
      val historyRoot = historySubquery.from(ReferralStatusHistoryEntity::class.java)
      val statusDescJoin =
        historyRoot.join<ReferralStatusHistoryEntity, ReferralStatusDescriptionEntity>("referralStatusDescription")

      historySubquery.select(cb.count(historyRoot))
      historySubquery.where(
        cb.equal(historyRoot.get<ReferralEntity>("referral"), referralJoin),
        cb.equal(statusDescJoin.get<String>("description"), "Programme complete"),
      )

      subquery.select(cb.count(membershipRoot))
      subquery.where(
        cb.equal(membershipRoot.get<ProgrammeGroupEntity>("programmeGroup"), root),
        cb.equal(historySubquery, 0L),
      )

      when (selectedTab) {
        GroupPageByRegionTab.NOT_STARTED_OR_IN_PROGRESS -> {
          val notStartedOrInProgressDateSpec = cb.or(
            cb.isNull(datePath),
            cb.greaterThan(datePath, LocalDate.now()),
          )

          cb.or(notStartedOrInProgressDateSpec, cb.greaterThan(subquery, 0L))
        }

        GroupPageByRegionTab.COMPLETE -> {
          val completeDateSpec = cb.lessThanOrEqualTo(datePath, LocalDate.now())

          cb.and(completeDateSpec, cb.equal(subquery, 0L))
        }
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

  fun getGroupSessionPage(groupId: UUID, sessionId: UUID): GroupSessionResponse {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id $groupId not found")

    val session = sessionRepository.findByIdOrNull(sessionId)
      ?: throw NotFoundException("Session with $sessionId not found")

    val attendanceAndSessionNotes = session.attendees.map { attendee ->
      val attendanceRecord = session.attendances
        .filter { it.groupMembership.referral.id == attendee.referralId }
        .sortedWith(compareByDescending<SessionAttendanceEntity> { it.createdAt }.thenByDescending { it.id })
        .firstOrNull()

      AttendanceAndSessionNotes(
        name = attendee.personName,
        referralId = attendee.referralId,
        crn = attendee.referral.crn,
        attendance = getAttendanceTextFromOutcome(attendanceRecord?.outcomeType),
        sessionNotes = attendanceRecord?.notesHistory?.maxByOrNull { it.createdAt }?.notes ?: "Not added",
      )
    }

    return GroupSessionResponse(
      groupCode = programmeGroup.code,
      pageTitle = sessionNameFormatter.format(session, SessionNameContext.SessionDetails),
      sessionType = session.sessionType.value,
      isCatchup = session.isCatchup,
      date = session.startsAt.toLocalDate(),
      time = formatTimeOfSession(session.startsAt.toLocalTime(), session.endsAt.toLocalTime()),
      scheduledToAttend = session.attendees.map { it.personName },
      facilitators = session.sessionFacilitators.sortedBy { it.facilitator.personName }
        .map { it.facilitator.personName },
      attendanceAndSessionNotes = attendanceAndSessionNotes,
    )
  }

  fun getAttendanceTextFromOutcome(attendanceOutcome: SessionAttendanceNDeliusOutcomeEntity?): String = when (attendanceOutcome?.code) {
    UAAB -> "Did not attend"
    null -> "To be confirmed"
    else -> attendanceOutcome.description!!
  }
}
