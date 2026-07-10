package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.govUkHolidaysApi.GovUkApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.AppointmentReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.DeleteAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toAppointment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.config.logToAppInsights
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toNdeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.toFacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.CREATE_APPOINTMENT_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.DELETE_APPOINTMENT_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.BankHolidayRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.PriorityQueue
import java.util.UUID

@Service
@Transactional
class ScheduleService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val moduleRepository: ModuleRepository,
  private val clock: Clock,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val govUkApiClient: GovUkApiClient,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val nDeliusAppointmentRepository: NDeliusAppointmentRepository,
  private val facilitatorService: FacilitatorService,
  private val referralRepository: ReferralRepository,
  private val sessionRepository: SessionRepository,
  private val sessionNameFormatter: SessionNameFormatter,
  private val bankHolidayRepository: BankHolidayRepository,
  private val telemetryClient: TelemetryClient,
) {

  private companion object {
    private const val POST_PROGRAMME_REVIEWS_MODULE_NAME = "Post-programme reviews"
    private const val FIRST_SESSION_GAP_WEEKS = 3L
    private const val POST_PROGRAMME_REVIEWS_GAP_WEEKS = 6L
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleIndividualSessionAndReturnResponse(groupId: UUID, request: ScheduleSessionRequest): String {
    val scheduledSession = scheduleIndividualSession(groupId, request)
    return sessionNameFormatter.format(scheduledSession, SessionNameContext.ScheduleIndividualSession)
  }

  fun scheduleIndividualSession(groupId: UUID, request: ScheduleSessionRequest): SessionEntity {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id: $groupId could not be found")

    val moduleSessionTemplate = moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId)
      ?: throw NotFoundException("Session template with id: ${request.sessionTemplateId} could not be found")

    val session = SessionEntity(
      programmeGroup = programmeGroup,
      moduleSessionTemplate = moduleSessionTemplate,
      startsAt = convertToLocalDateTime(request.startDate, request.startTime),
      endsAt = convertToLocalDateTime(request.startDate, request.endTime),
      locationName = programmeGroup.deliveryLocationName,
      // When scheduling session it should not be a placeholder
      isPlaceholder = false,
      isCatchup = request.sessionScheduleType.isCatchUp,
    )
    val sessionFacilitators = request.facilitators.map {
      SessionFacilitatorEntity(
        facilitator = facilitatorService.findOrCreateFacilitator(it),
        session = session,
        facilitatorType = it.teamMemberType.toFacilitatorType(),
      )
    }.toMutableSet()
    session.sessionFacilitators = sessionFacilitators

    request.referralIds.forEach { referralId ->
      val referral = referralRepository.findByIdOrNull(referralId)
        ?: throw NotFoundException("Referral with id: $referralId could not be found")
      session.attendees.add(
        AttendeeEntity(
          referral = referral,
          session = session,
        ),
      )
    }

    val savedSession = sessionRepository.save(session)
    val sessionAttendees = session.attendees
    if (sessionAttendees.isNotEmpty()) {
      createNdeliusAppointmentsForSessions(sessionAttendees)
    }

    return savedSession
  }

  fun getNextSlotDate(programmeGroupId: UUID, moduleId: UUID): LocalDate {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
      ?: throw NotFoundException("Group with id: $programmeGroupId could not be found")

    // Pre group should be the date of the first session in the Pre Group Module, or the placeholder date when there are no sessions
    if (moduleSessionTemplateRepository.isAPreGroupSession(moduleId)) {
      val sessions = sessionRepository.findByProgrammeGroupId(programmeGroupId)
      return sessions
        .filter { it.moduleSessionTemplate.module.isPreGroupModule() }
        .minByOrNull { it.startsAt }
        ?.startsAt!!.toLocalDate()
    }

    // Post programme should use the next date after the final group plus a 6-week buffer
    else if (moduleSessionTemplateRepository.isAPostProgrammeSession(moduleId)) {
      val sessions = sessionRepository.findByProgrammeGroupId(programmeGroupId)
      return sessions
        .maxByOrNull { it.startsAt }
        ?.startsAt!!.toLocalDate()
    }

    // Otherwise get the date of the last scheduled session (not a catch up) for the module and calculate the next date based on that
    val groupModuleSessions = moduleSessionTemplateRepository.findByModuleIdAndNotOneToOne(moduleId)
    log.debug("Found {} session templates for module: {}", groupModuleSessions.size, moduleId)

    val sessionsToCheck = mutableListOf<SessionEntity>()
    groupModuleSessions.forEach { groupModuleSessionId ->
      val sessions = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupIdWhenNotCatchUp(
        moduleSessionTemplateId = groupModuleSessionId,
        programmeGroupId = programmeGroupId,
      )
      sessionsToCheck.addAll(sessions)
    }

    val lastScheduledSession = sessionsToCheck.maxByOrNull { it.startsAt }
    val dateToScheduleFrom = lastScheduledSession!!.startsAt.toLocalDate().plusDays(1)

    val groupSlots = group.programmeGroupSessionSlots
    require(groupSlots.isNotEmpty()) { "Programme group slots must not be empty" }

    val bankHolidays = englandAndWalesHolidayDates()

    val slotQueue = buildSlotQueue(
      bankHolidays = bankHolidays,
      groupSlots = groupSlots,
      startFrom = dateToScheduleFrom,
    )

    val nextSlot = slotQueue.poll()
    val nextValidDate = findNextValidDate(bankHolidays, dateToScheduleFrom, nextSlot.slot)
    slotQueue.add(nextSlot.copy(nextDate = nextValidDate))

    log.debug("generated next valid slot date: {}", nextValidDate)
    return nextValidDate
  }

  fun scheduleSessionsForGroup(
    programmeGroupId: UUID,
    mostRecentSession: SessionEntity? = null,
    skipPreGroupOneToOnePlaceholder: Boolean = false,
    coveredTemplateIds: Set<UUID> = emptySet(),
    preventPastScheduling: Boolean = false,
  ): MutableSet<SessionEntity> {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
      ?: throw NotFoundException("Group with id: $programmeGroupId could not be found")

    val templateId = requireNotNull(group.accreditedProgrammeTemplate?.id) {
      "Group template Id must not be null"
    }
    val bankHolidays = englandAndWalesHolidayDates()

    // Collect all session templates in module/session order
    var allSessionTemplates = moduleRepository
      .findByAccreditedProgrammeTemplateId(templateId)
      .sortedBy { it.moduleNumber }
      .flatMap { moduleEntity ->
        moduleEntity.sessionTemplates.sortedBy { it.sessionNumber }
      }

    // If rescheduling, only regenerate templates that are not already covered by a session we are
    // keeping (i.e. a past session, or a retained future placeholder). This ensures we do not drop future sessions
    // that are sit earlier in the template order than the most recent past session
    if (coveredTemplateIds.isNotEmpty()) {
      allSessionTemplates = allSessionTemplates.filter { template -> template.id !in coveredTemplateIds }
    }

    if (skipPreGroupOneToOnePlaceholder) {
      allSessionTemplates = allSessionTemplates.filter { template ->
        !(template.sessionType == ONE_TO_ONE && template.module.name == "Pre-group one-to-ones")
      }
    }

    val groupSlots = group.programmeGroupSessionSlots
    require(groupSlots.isNotEmpty()) { "Programme group slots must not be empty" }

    // When rescheduling after past sessions, start from the later of:
    // - the day after the most recent past session (so future sessions are never placed before it),
    // - the group's configured earliest start date (honoured when e.g. a future restart date was set), or
    // - today (so regenerated sessions are never placed in the past, even when the most recent past
    //   session ran several days ago and the new slot day falls between then and now)
    val startFrom = if (mostRecentSession != null) {
      maxOf(
        mostRecentSession.startsAt.toLocalDate().plusDays(1),
        group.earliestPossibleStartDate,
        LocalDate.now(clock),
      )
    } else if (preventPastScheduling) {
      // Rescheduling a group with no past session to anchor on (e.g. a membership group whose
      // sessions are all still in the future) must never place sessions in the past, even when the
      // group's start date has already passed. Empty groups and initial scheduling leave this false
      // so in-flight imports can still be generated from a past start date.
      maxOf(group.earliestPossibleStartDate, LocalDate.now(clock))
    } else {
      group.earliestPossibleStartDate
    }

    var slotQueue = buildSlotQueue(
      bankHolidays = bankHolidays,
      groupSlots = groupSlots,
      startFrom = startFrom,
    )

    var firstSessionScheduledAndGapApplied = false
    var postProgrammeReviewsGapApplied = false

    val generatedSessions = buildList {
      for (template in allSessionTemplates) {
        val nextSlot = slotQueue.poll() ?: break

        val startsAt = LocalDateTime.of(nextSlot.nextDate, nextSlot.slot.startTime)
        val endsAt = startsAt.plusMinutes(template.durationMinutes.toLong())

        val session = SessionEntity(
          programmeGroup = group,
          moduleSessionTemplate = template,
          startsAt = startsAt,
          endsAt = endsAt,
          locationName = group.deliveryLocationName,
          // If we are scheduling One-to-One here, it is a placeholder session
          isPlaceholder = template.sessionType == ONE_TO_ONE,
        )
        session.sessionFacilitators = group.groupFacilitators.map {
          SessionFacilitatorEntity(
            facilitator = it.facilitator,
            session,
            facilitatorType = it.facilitatorType,
          )
        }.toMutableSet()
        add(session)

        if (
          mostRecentSession == null &&
          !firstSessionScheduledAndGapApplied &&
          template.module.moduleNumber == 1 &&
          template.sessionNumber == 1
        ) {
          firstSessionScheduledAndGapApplied = true

          // Regenerate our slot queue plus 3 weeks
          slotQueue = buildSlotQueue(
            bankHolidays = bankHolidays,
            groupSlots = groupSlots,
            startFrom = startsAt.toLocalDate().plusWeeks(FIRST_SESSION_GAP_WEEKS),
          )
        } else {
          val nextWeekDate = nextSlot.nextDate.plusWeeks(1)
          val nextValidDate = findNextValidDate(bankHolidays, nextWeekDate, nextSlot.slot)
          slotQueue.add(nextSlot.copy(nextDate = nextValidDate))
        }

        if (!postProgrammeReviewsGapApplied && template.module.name != POST_PROGRAMME_REVIEWS_MODULE_NAME) {
          val nextTemplate = allSessionTemplates.getOrNull(allSessionTemplates.indexOf(template) + 1)
          if (nextTemplate?.module?.name == POST_PROGRAMME_REVIEWS_MODULE_NAME) {
            postProgrammeReviewsGapApplied = true

            // Regenerate our slot queue plus 6 weeks
            slotQueue = buildSlotQueue(
              bankHolidays = bankHolidays,
              groupSlots = groupSlots,
              startFrom = startsAt.toLocalDate().plusWeeks(POST_PROGRAMME_REVIEWS_GAP_WEEKS),
            )
          }
        }
      }
    }

    log.debug(
      "Generated {} sessions for group code={}, programmeGroupId={}, templateId={}",
      generatedSessions.size,
      group.code,
      programmeGroupId,
      templateId,
    )

    val programmeGroupMemberships =
      programmeGroupMembershipRepository.findAllByProgrammeGroupIdAndDeletedAtIsNullOrderByCreatedAtDesc(group.id!!)

    if (programmeGroupMemberships.isNotEmpty()) {
      programmeGroupMemberships.forEach { groupMembership ->
        generatedSessions.forEach { session ->
          session.attendees.add(
            AttendeeEntity(
              referral = groupMembership.referral,
              session = session,
            ),
          )
        }
      }
    }

    // Save the group here so the parent entity is updated and updates the corresponding sessions,
    // otherwise JPA does not always update the inverse side of the relationship.
    group.sessions.addAll(generatedSessions)
    programmeGroupRepository.save(group)

    return group.sessions
  }

  private fun buildSlotQueue(
    bankHolidays: Set<LocalDate>,
    groupSlots: Collection<ProgrammeGroupSessionSlotEntity>,
    startFrom: LocalDate,
  ): PriorityQueue<SlotInstance> {
    // Priority by (nextDate ASC, startTime ASC)
    val slotQueue = PriorityQueue(compareBy<SlotInstance>({ it.nextDate }, { it.slot.startTime }))
    groupSlots.forEach { slot ->
      val firstDate = findNextValidDate(bankHolidays, startFrom, slot)
      slotQueue.add(SlotInstance(slot = slot, nextDate = firstDate))
    }
    return slotQueue
  }

  fun rescheduleSessionsForGroup(
    programmeGroupId: UUID,
    skipPreGroupOneToOnePlaceholder: Boolean = false,
  ): MutableSet<SessionEntity> {
    val group = requireNotNull(programmeGroupRepository.findByIdOrNull(programmeGroupId)) {
      "Group must not be null"
    }
    requireNotNull(group.earliestPossibleStartDate) {
      "Earliest start date must not be null"
    }

    val now = LocalDateTime.now(clock)

    // Empty groups (no membership, ever) regenerate the whole schedule from the group's start date,
    // including sessions currently in the past. This supports migrating in-flight groups created with
    // an early start date. Groups with members keep their past sessions and only reschedule the future.
    val isEmptyGroup = !programmeGroupMembershipRepository.existsByProgrammeGroupId(programmeGroupId)

    var sessionsToReschedule = if (isEmptyGroup) {
      group.sessions.toSet()
    } else {
      group.sessions.filter { it.startsAt > now }.toSet()
    }
    if (skipPreGroupOneToOnePlaceholder) {
      sessionsToReschedule =
        sessionsToReschedule.filterNot { it.moduleName == "Pre-group one-to-ones" && it.isPlaceholder }.toSet()
    }
    val nDeliusAppointmentsToRemove = sessionsToReschedule.flatMap { session -> session.ndeliusAppointments }

    // Anchor the regenerated schedule after the most recent past session for groups with members; for
    // empty groups schedule from the start date (null), so past sessions are regenerated too.
    val mostRecentSession = if (isEmptyGroup) {
      null
    } else {
      group.sessions.filter { it.startsAt <= now }.maxByOrNull { it.startsAt }
    }

    // Templates already covered by a session we are keeping (past sessions, plus any retained future
    // placeholder) must not be regenerated. Catch-ups are excluded.
    val retainedSessions = group.sessions.filterNot { it in sessionsToReschedule }
    val coveredTemplateIds = retainedSessions
      .filterNot { it.isCatchup }
      .mapNotNull { it.moduleSessionTemplate.id }
      .toSet()

    if (sessionsToReschedule.isNotEmpty()) {
      removeNDeliusAppointments(nDeliusAppointmentsToRemove, sessionsToReschedule.toList())
      group.sessions.removeAll(sessionsToReschedule)
      programmeGroupRepository.save(group)
    }

    // If there is no prior session, just schedule from start
    val sessions = scheduleSessionsForGroup(
      programmeGroupId,
      mostRecentSession,
      skipPreGroupOneToOnePlaceholder,
      coveredTemplateIds,
      // Empty groups may regenerate into the past (in-flight import); groups with members never may.
      preventPastScheduling = !isEmptyGroup,
    )
    val attendees = sessions.flatMap { it.attendees }.toList()
    if (attendees.isNotEmpty()) {
      createNdeliusAppointmentsForSessions(attendees)
    }

    return sessions
  }

  fun createNdeliusAppointmentsForSessions(attendees: List<AttendeeEntity>) {
    // Never call nDelius with an empty payload (e.g. rescheduling an empty group has no attendees).
    if (attendees.isEmpty()) return

    val (nDeliusAppointments, nDeliusAppointmentEntities) = attendees.map { attendee ->
      // Generate an appointment ID to be used by NDelius
      val appointmentId = UUID.randomUUID()
      val appointment = attendee.toAppointment(appointmentId)
      val appointmentEntity = attendee.toNdeliusAppointmentEntity(appointmentId)
      appointment to appointmentEntity
    }.unzip()

    // Extract CRNs and event numbers for diagnostic logging
    val affectedCrns = attendees.map { it.referral.crn }.distinct()
    val affectedEventNumbers = attendees.map { "${it.referral.crn}:${it.referral.eventNumber}" }.distinct()
    val groupId = attendees.firstOrNull()?.session?.programmeGroup?.id

    when (
      val response =
        nDeliusIntegrationApiClient.createAppointmentsInDelius(CreateAppointmentRequest(nDeliusAppointments))
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.error(
          "Failure to create nDelius appointments — status: ${response.status}, " +
            "path: ${response.path}, groupId: $groupId, " +
            "CRNs: $affectedCrns, eventNumbers: $affectedEventNumbers, " +
            "responseBody: ${response.body}",
        )
        telemetryClient.logToAppInsights(
          "${CREATE_APPOINTMENT_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
            "statusCode" to response.status.toString(),
            "crns" to affectedCrns.joinToString(","),
            "eventNumbers" to affectedEventNumbers.joinToString(","),
            "groupId" to (groupId?.toString() ?: ""),
            "responseBody" to (response.body?.take(500) ?: ""),
          ),
        )

        // Fire a finer-grained telemetry event when nDelius rejects the appointment
        // because the linked requirement / licence condition has been terminated.
        // The generic .failure event above still fires — this is an additional slice
        // for observability, not a replacement.
        if (response.body?.contains("Invalid Requirement IDs") == true) {
          telemetryClient.logToAppInsights(
            "${CREATE_APPOINTMENT_N_DELIUS.eventName}.terminated-requirement",
            mapOf(
              "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
              "outcome" to "terminated-requirement",
              "crns" to affectedCrns.joinToString(","),
              "eventNumbers" to affectedEventNumbers.joinToString(","),
              "groupId" to (groupId?.toString() ?: ""),
            ),
          )
        }

        throw BusinessException("Failure to create appointments", response.toException())
      }

      is ClientResult.Failure.Other -> {
        log.error(
          "Failure to create nDelius appointments — service: ${response.serviceName}, " +
            "groupId: $groupId, CRNs: $affectedCrns, eventNumbers: $affectedEventNumbers, " +
            "exception: ${response.exception.message}",
          response.exception,
        )
        telemetryClient.logToAppInsights(
          "${CREATE_APPOINTMENT_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
            "crns" to affectedCrns.joinToString(","),
            "eventNumbers" to affectedEventNumbers.joinToString(","),
            "groupId" to (groupId?.toString() ?: ""),
            "errorMessage" to (response.exception.message ?: "unknown"),
          ),
        )
        throw BusinessException(
          "Failure to create appointments in nDelius: ${response.exception.message}",
          response.exception,
        )
      }

      is ClientResult.Success -> {
        log.info("${nDeliusAppointments.size} appointments created in nDelius for groupId: $groupId, CRNs: $affectedCrns")
        telemetryClient.logToAppInsights(
          "${CREATE_APPOINTMENT_N_DELIUS.eventName}.success",
          mapOf(
            "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "success",
            "appointmentCount" to nDeliusAppointments.size.toString(),
            "groupId" to (groupId?.toString() ?: ""),
          ),
        )

        nDeliusAppointmentRepository.saveAll(nDeliusAppointmentEntities)
      }
    }
  }

  /**
   * Finds the next valid date (not a bank holiday) starting from the given date,
   * on or after the slot's day of week.
   */
  private fun findNextValidDate(
    bankHolidays: Set<LocalDate>,
    fromDate: LocalDate,
    slot: ProgrammeGroupSessionSlotEntity,
  ): LocalDate {
    var candidateDate = fromDate.with(TemporalAdjusters.nextOrSame(slot.dayOfWeek))

    // Keep advancing by weeks until we find a non-holiday
    while (bankHolidays.contains(candidateDate)) {
      candidateDate = candidateDate.plusWeeks(1)
    }

    return candidateDate
  }

  private data class SlotInstance(
    val slot: ProgrammeGroupSessionSlotEntity,
    val nextDate: LocalDate,
  )

  fun englandAndWalesHolidayDates(): Set<LocalDate> = bankHolidayRepository.findAll()
    .map { it.holidayDate }
    .toSet()

  /**
   * Generates the new dates for an ordered list of sessions being rescheduled (in module/session
   * template order), placing each on the group's slots. Each session keeps its own template
   * duration, and the six-week gap before the first Post-programme reviews session is re-applied so
   * the cascade preserves the same spacing as the initial schedule.
   */
  fun generateScheduleDates(
    programmeGroupSlots: Collection<ProgrammeGroupSessionSlotEntity>,
    initialStartDate: LocalDate,
    subsequentSessions: List<SessionEntity>,
    bankHolidays: Set<LocalDate>,
  ): List<Pair<LocalDateTime, LocalDateTime>> {
    // Start from the day after the rescheduled session so that subsequent sessions
    // are placed on the next available slots.  Building from the initialStartDate + 1 day
    // works whether the rescheduled session falls on a slot day or not: when it IS a
    // slot day, the slot is naturally skipped; when it is NOT a slot day, the first
    // available slot (which belongs to the next session) is no longer incorrectly
    // consumed as if it were the rescheduled session's own slot.
    var slotQueue = buildSlotQueue(bankHolidays, programmeGroupSlots, initialStartDate.plusDays(1))
    val schedule = mutableListOf<Pair<LocalDateTime, LocalDateTime>>()
    var postProgrammeReviewsGapApplied = false

    subsequentSessions.forEachIndexed { index, sessionToPlace ->
      val slotInstance = slotQueue.poll()
      val startsAt = slotInstance.nextDate.atTime(slotInstance.slot.startTime)
      val endsAt = startsAt.plusMinutes(sessionToPlace.moduleSessionTemplate.durationMinutes.toLong())
      schedule.add(startsAt to endsAt)

      // Re-apply the six-week gap before the first Post-programme reviews session, mirroring the
      // initial scheduling in scheduleSessionsForGroup so the cascade keeps the correct spacing.
      val nextSession = subsequentSessions.getOrNull(index + 1)
      if (
        !postProgrammeReviewsGapApplied &&
        sessionToPlace.moduleName != POST_PROGRAMME_REVIEWS_MODULE_NAME &&
        nextSession?.moduleName == POST_PROGRAMME_REVIEWS_MODULE_NAME
      ) {
        postProgrammeReviewsGapApplied = true
        slotQueue = buildSlotQueue(
          bankHolidays = bankHolidays,
          groupSlots = programmeGroupSlots,
          startFrom = startsAt.toLocalDate().plusWeeks(POST_PROGRAMME_REVIEWS_GAP_WEEKS),
        )
      } else {
        val nextDate = findNextValidDate(bankHolidays, slotInstance.nextDate.plusDays(1), slotInstance.slot)
        slotQueue.add(slotInstance.copy(nextDate = nextDate))
      }
    }

    return schedule
  }

  private fun englandAndWalesHolidayDatesFromApi(): Set<LocalDate> = when (val response = govUkApiClient.getHolidays()) {
    is ClientResult.Failure.StatusCode -> {
      log.warn("Failed to retrieve UK bank holidays - Status: ${response.status}, Path: ${response.path}, Body: ${response.body}")
      throw BusinessException(
        "Could not retrieve bank holidays from GovUk Api. Status: ${response.status}",
        response.toException(),
      )
    }

    is ClientResult.Failure.Other -> {
      log.warn(
        "Failed to retrieve UK bank holidays - Service: ${response.serviceName}, Exception: ${response.exception.message}",
        response.exception,
      )
      throw BusinessException(
        "Could not retrieve bank holidays from GovUk Api: ${response.exception.message}",
        response.exception,
      )
    }

    is ClientResult.Success -> {
      log.debug("Successfully retrieved UK bank holidays...")
      response.body.englandAndWales.events
        .mapNotNull { event ->
          runCatching { LocalDate.parse(event.date) }.getOrNull()
        }
        .toSet()
    }
  }

  private fun convertToLocalDateTime(startDate: LocalDate, sessionTime: SessionTime): LocalDateTime = LocalDateTime.of(
    startDate,
    LocalTime.of(
      when (sessionTime.amOrPm) {
        AmOrPm.PM if sessionTime.hour < 12 -> sessionTime.hour + 12
        AmOrPm.AM if sessionTime.hour == 12 -> 0
        else -> sessionTime.hour
      },
      sessionTime.minutes,
    ),
  )

  fun removeNDeliusAppointments(
    nDeliusAppointmentsToRemove: List<NDeliusAppointmentEntity>,
    sessions: List<SessionEntity>,
  ) {
    if (nDeliusAppointmentsToRemove.isEmpty()) return

    when (
      val response = nDeliusIntegrationApiClient.deleteAppointmentsInDelius(
        DeleteAppointmentsRequest(
          appointments = nDeliusAppointmentsToRemove.map { AppointmentReference(it.ndeliusAppointmentId) },
        ),
      )
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.warn(
          "Failure deleting appointments in nDelius with reason: ${response.getErrorMessage()}",
          response.toException(),
        )
        telemetryClient.logToAppInsights(
          "${DELETE_APPOINTMENT_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to DELETE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
          ),
        )
        throw BusinessException(
          "Failure deleting appointments in nDelius with status code : ${response.status}",
          response.toException(),
        )
      }

      is ClientResult.Failure.Other -> {
        log.warn(
          "Failure to delete appointments - Service: ${response.serviceName}, Exception: ${response.exception.message}",
          response.exception,
        )
        telemetryClient.logToAppInsights(
          "${DELETE_APPOINTMENT_N_DELIUS.eventName}.failure",
          mapOf(
            "integrationActionType" to DELETE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
          ),
        )
        throw BusinessException(
          "Failure to delete appointments in NDelius: ${response.exception.message}",
          response.exception,
        )
      }

      is ClientResult.Success -> {
        sessions.forEach { session ->
          session.ndeliusAppointments
            .removeIf { appointment -> appointment.ndeliusAppointmentId in nDeliusAppointmentsToRemove.map { it.ndeliusAppointmentId } }
        }
        log.info("${nDeliusAppointmentsToRemove.size} appointments deleted in NDelius")
        telemetryClient.logToAppInsights(
          "${DELETE_APPOINTMENT_N_DELIUS.eventName}.success",
          mapOf(
            "integrationActionType" to DELETE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "success",
          ),
        )
      }
    }
  }
}
