package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.toLocalTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.ClientResult
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.NDeliusIntegrationApiClient
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.AppointmentReference
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.CreateAppointmentRequest.NdeliusAppointment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.DeleteAppointmentsRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.UpdateAppointmentRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.client.nDeliusIntegrationApi.model.toAppointment
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionFacilitatorEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.currentSessionNotes
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toNdeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType.ONE_TO_ONE
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.toFacilitatorType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.CREATE_APPOINTMENT_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.IntegrationActivityType.DELETE_APPOINTMENT_N_DELIUS
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.BankHolidayRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameContext
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils.SessionNameFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.PriorityQueue
import java.util.UUID

@Service
@Transactional
class ScheduleService(
  private val programmeGroupRepository: ProgrammeGroupRepository,
  private val moduleRepository: ModuleRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val nDeliusIntegrationApiClient: NDeliusIntegrationApiClient,
  private val nDeliusAppointmentRepository: NDeliusAppointmentRepository,
  private val facilitatorService: FacilitatorService,
  private val referralRepository: ReferralRepository,
  private val sessionRepository: SessionRepository,
  private val sessionNameFormatter: SessionNameFormatter,
  private val bankHolidayRepository: BankHolidayRepository,
  private val telemetryService: TelemetryService,
) {

  private companion object {
    private const val POST_PROGRAMME_REVIEWS_MODULE_NAME = "Post-programme reviews"
    private const val FIRST_SESSION_GAP_WEEKS = 3L
    private const val POST_PROGRAMME_REVIEWS_GAP_WEEKS = 6L
  }

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleSessionAndReturnResponse(groupId: UUID, request: ScheduleSessionRequest): String {
    val scheduledSession = scheduleSession(groupId, request)
    return sessionNameFormatter.format(scheduledSession, SessionNameContext.ScheduleSession)
  }

  fun scheduleSession(groupId: UUID, request: ScheduleSessionRequest): SessionEntity {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id: $groupId could not be found")

    val moduleSessionTemplate = moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId)
      ?: throw NotFoundException("Session template with id: ${request.sessionTemplateId} could not be found")

    val session = SessionEntity(
      programmeGroup = programmeGroup,
      moduleSessionTemplate = moduleSessionTemplate,
      startsAt = LocalDateTime.of(request.startDate, request.startTime.toLocalTime()),
      endsAt = LocalDateTime.of(request.startDate, request.endTime.toLocalTime()),
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

    // Otherwise get the date of the last scheduled session (not a catch-up) for the module and calculate the next date based on that
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

    val nextValidDate = buildSlotQueue(
      bankHolidays = englandAndWalesHolidayDates(),
      groupSlots = groupSlots,
      startFrom = dateToScheduleFrom,
    ).poll().nextDate

    log.debug("generated next valid slot date: {}", nextValidDate)
    return nextValidDate
  }

  fun scheduleSessionsForGroup(programmeGroupId: UUID) {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
      ?: throw NotFoundException("Group with id: $programmeGroupId could not be found")

    val templateId = requireNotNull(group.accreditedProgrammeTemplate?.id) {
      "Group template Id must not be null"
    }

    // Create a session for each session template, in module/session order
    val generatedSessions = moduleRepository
      .findByAccreditedProgrammeTemplateId(templateId)
      .sortedBy { it.moduleNumber }
      .flatMap { moduleEntity ->
        moduleEntity.sessionTemplates.sortedBy { it.sessionNumber }
      }
      .map { template ->
        val session = SessionEntity(
          programmeGroup = group,
          moduleSessionTemplate = template,
          // The real dates are set when the sessions are moved onto the group's slots below
          startsAt = group.earliestPossibleStartDate.atStartOfDay(),
          endsAt = group.earliestPossibleStartDate.atStartOfDay(),
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
        session
      }

    moveSessionsOntoSlots(group.programmeGroupSessionSlots, generatedSessions, group.earliestPossibleStartDate)

    log.debug(
      "Generated {} sessions for group code={}, programmeGroupId={}, templateId={}",
      generatedSessions.size,
      group.code,
      programmeGroupId,
      templateId,
    )

    // Save the group here so the parent entity is updated and updates the corresponding sessions,
    // otherwise JPA does not always update the inverse side of the relationship.
    group.sessions.addAll(generatedSessions)
    programmeGroupRepository.save(group)
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

  fun recreateMissingNDeliusAppointments(
    nDeliusAppointmentEntities: List<NDeliusAppointmentEntity>,
    updateAppointmentRequests: List<UpdateAppointmentRequest>,
  ) {
    if (nDeliusAppointmentEntities.isEmpty() || updateAppointmentRequests.isEmpty()) return

    val nDeliusAppointments = nDeliusAppointmentEntities.map { nDeliusAppointmentEntity ->
      val updateAppointmentRequest = updateAppointmentRequests.first { request ->
        request.reference == nDeliusAppointmentEntity.ndeliusAppointmentId
      }
      // Contacts are soft-deleted in delius when sent from our service so we cannot use the original UUID to recreate the contact
      nDeliusAppointmentEntity.ndeliusAppointmentId = UUID.randomUUID()
      nDeliusAppointmentEntity.toAppointment(
        sessionNameFormatter = sessionNameFormatter,
        notes = updateAppointmentRequest.notes ?: nDeliusAppointmentEntity.currentSessionNotes(),
        outcome = updateAppointmentRequest.outcome,
      )
    }

    val affectedCaseReferenceNumbers = nDeliusAppointmentEntities.map { it.referral.crn }.distinct()
    val affectedEventNumbers =
      nDeliusAppointmentEntities.map { "${it.referral.crn}:${it.referral.eventNumber}" }.distinct()
    val groupId = nDeliusAppointmentEntities.firstOrNull()?.session?.programmeGroup?.id

    createMissingNDeliusAppointments(
      nDeliusAppointments = nDeliusAppointments,
      nDeliusAppointmentEntities = nDeliusAppointmentEntities,
      affectedCaseReferenceNumbers = affectedCaseReferenceNumbers,
      affectedEventNumbers = affectedEventNumbers,
      groupId = groupId,
    )
  }

  fun createNdeliusAppointmentsForSessions(attendees: List<AttendeeEntity>) {
    // Never call nDelius with an empty payload (e.g. rescheduling an empty group has no attendees).
    if (attendees.isEmpty()) return

    val (nDeliusAppointments, nDeliusAppointmentEntities) = attendees.map { attendee ->
      // Generate an appointment ID to be used by NDelius
      val appointmentId = UUID.randomUUID()
      val appointment = attendee.toAppointment(appointmentId, sessionNameFormatter)
      val appointmentEntity = attendee.toNdeliusAppointmentEntity(appointmentId)
      appointment to appointmentEntity
    }.unzip()

    // Extract CRNs and event numbers for diagnostic logging
    val affectedCaseReferenceNumbers = attendees.map { it.referral.crn }.distinct()
    val affectedEventNumbers = attendees.map { "${it.referral.crn}:${it.referral.eventNumber}" }.distinct()
    val groupId = attendees.firstOrNull()?.session?.programmeGroup?.id

    createMissingNDeliusAppointments(
      nDeliusAppointments = nDeliusAppointments,
      nDeliusAppointmentEntities = nDeliusAppointmentEntities,
      affectedCaseReferenceNumbers = affectedCaseReferenceNumbers,
      affectedEventNumbers = affectedEventNumbers,
      groupId = groupId,
    )
  }

  private fun createMissingNDeliusAppointments(
    nDeliusAppointments: List<NdeliusAppointment>,
    nDeliusAppointmentEntities: List<NDeliusAppointmentEntity>,
    affectedCaseReferenceNumbers: List<String>,
    affectedEventNumbers: List<String>,
    groupId: UUID?,
  ) {
    when (
      val response =
        nDeliusIntegrationApiClient.createAppointmentsInDelius(CreateAppointmentRequest(nDeliusAppointments))
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.error(
          "Failure to create nDelius appointments — status: ${response.status}, " +
            "path: ${response.path}, groupId: $groupId, " +
            "CRNs: $affectedCaseReferenceNumbers, eventNumbers: $affectedEventNumbers, " +
            "responseBody: ${response.body}",
        )
        telemetryService.logToAppInsights(
          eventName = "${CREATE_APPOINTMENT_N_DELIUS.eventName}.failure",
          properties = mapOf(
            "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
            "statusCode" to response.status.toString(),
            "crns" to affectedCaseReferenceNumbers.joinToString(","),
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
          telemetryService.logToAppInsights(
            eventName = "${CREATE_APPOINTMENT_N_DELIUS.eventName}.terminated-requirement",
            properties = mapOf(
              "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
              "outcome" to "terminated-requirement",
              "crns" to affectedCaseReferenceNumbers.joinToString(","),
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
            "groupId: $groupId, CRNs: $affectedCaseReferenceNumbers, eventNumbers: $affectedEventNumbers, " +
            "exception: ${response.exception.message}",
          response.exception,
        )
        telemetryService.logToAppInsights(
          eventName = "${CREATE_APPOINTMENT_N_DELIUS.eventName}.failure",
          properties = mapOf(
            "integrationActionType" to CREATE_APPOINTMENT_N_DELIUS.name,
            "outcome" to "failure",
            "crns" to affectedCaseReferenceNumbers.joinToString(","),
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
        log.info("${nDeliusAppointments.size} appointments created in nDelius for groupId: $groupId, CRNs: $affectedCaseReferenceNumbers")
        telemetryService.logToAppInsights(
          eventName = "${CREATE_APPOINTMENT_N_DELIUS.eventName}.success",
          properties = mapOf(
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
   * Places sessions on a group's slots. Moves [sessions], in the order given, onto [slots]:
   * one session per slot from [startFrom] onwards, skipping bank holidays. The programme's gaps are kept:
   * three weeks after the pre-group one-to-ones, and six weeks before the first post-programme review.
   *
   * Only each session's date and time change, and each keeps its template's duration. Attendees, facilitators and
   * nDelius contacts are untouched, so a caller moving sessions with nDelius contacts must update them afterwards.
   */
  fun moveSessionsOntoSlots(
    slots: Collection<ProgrammeGroupSessionSlotEntity>,
    sessions: List<SessionEntity>,
    startFrom: LocalDate,
  ) {
    require(slots.isNotEmpty()) { "Programme group slots must not be empty" }

    val bankHolidays = englandAndWalesHolidayDates()
    var slotQueue = buildSlotQueue(bankHolidays, slots, startFrom)
    var preGroupGapApplied = false
    var postProgrammeReviewsGapApplied = false

    sessions.forEachIndexed { index, session ->
      val slotInstance = slotQueue.poll()
      session.startsAt = slotInstance.nextDate.atTime(slotInstance.slot.startTime)
      session.endsAt = session.startsAt.plusMinutes(session.moduleSessionTemplate.durationMinutes.toLong())

      val nextSession = sessions.getOrNull(index + 1)
      if (
        !postProgrammeReviewsGapApplied &&
        session.moduleName != POST_PROGRAMME_REVIEWS_MODULE_NAME &&
        nextSession?.moduleName == POST_PROGRAMME_REVIEWS_MODULE_NAME
      ) {
        postProgrammeReviewsGapApplied = true
        slotQueue = buildSlotQueue(
          bankHolidays = bankHolidays,
          groupSlots = slots,
          startFrom = session.startsAt.toLocalDate().plusWeeks(POST_PROGRAMME_REVIEWS_GAP_WEEKS),
        )
      } else if (!preGroupGapApplied && session.moduleNumber == 1 && session.sessionNumber == 1) {
        preGroupGapApplied = true
        slotQueue = buildSlotQueue(
          bankHolidays = bankHolidays,
          groupSlots = slots,
          startFrom = session.startsAt.toLocalDate().plusWeeks(FIRST_SESSION_GAP_WEEKS),
        )
      } else {
        val nextDate = findNextValidDate(bankHolidays, slotInstance.nextDate.plusDays(1), slotInstance.slot)
        slotQueue.add(slotInstance.copy(nextDate = nextDate))
      }
    }
  }

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
        telemetryService.logToAppInsights(
          eventName = "${DELETE_APPOINTMENT_N_DELIUS.eventName}.failure",
          integrationActionType = DELETE_APPOINTMENT_N_DELIUS.name,
          outcome = "failure",
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
        telemetryService.logToAppInsights(
          eventName = "${DELETE_APPOINTMENT_N_DELIUS.eventName}.failure",
          integrationActionType = DELETE_APPOINTMENT_N_DELIUS.name,
          outcome = "failure",
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
        telemetryService.logToAppInsights(
          eventName = "${DELETE_APPOINTMENT_N_DELIUS.eventName}.success",
          integrationActionType = DELETE_APPOINTMENT_N_DELIUS.name,
          outcome = "success",
        )
      }
    }
  }
}
