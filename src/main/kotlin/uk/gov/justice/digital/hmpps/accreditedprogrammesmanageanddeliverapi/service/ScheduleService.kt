package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionResponse
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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.AttendeeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.NDeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.toNdeliusAppointmentEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionType
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.NDeliusAppointmentRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
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
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleIndividualSession(groupId: UUID, request: ScheduleSessionRequest): ScheduleSessionResponse {
    val programmeGroup = programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id: $groupId could not be found")

    val moduleSessionTemplate = moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId)
      ?: throw NotFoundException("Session template with id: ${request.sessionTemplateId} could not be found")

    val sessionFacilitators = request.facilitators.map {
      facilitatorService.findOrCreateFacilitator(it)
    }.toMutableSet()

    val session = SessionEntity(
      programmeGroup = programmeGroup,
      moduleSessionTemplate = moduleSessionTemplate,
      startsAt = convertToLocalDateTime(request.startDate, request.startTime),
      endsAt = convertToLocalDateTime(request.startDate, request.endTime),
      locationName = programmeGroup.deliveryLocationName,
      sessionFacilitators = sessionFacilitators,
      // Scheduling individual session should not be placeholder
      isPlaceholder = false,
    )

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
    createNdeliusAppointmentsForSessions(savedSession.attendees)

    return ScheduleSessionResponse(message = "Session scheduled successfully")
  }

  fun scheduleSessionsForGroup(
    programmeGroupId: UUID,
    mostRecentSession: SessionEntity? = null,
  ): MutableSet<SessionEntity> {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
      ?: throw NotFoundException("Group with id: $programmeGroupId could not be found")

    val templateId = requireNotNull(group.accreditedProgrammeTemplate?.id) {
      "Group template Id must not be null"
    }

    // Collect all session templates in module/session order
    var allSessionTemplates = moduleRepository
      .findByAccreditedProgrammeTemplateId(templateId)
      .sortedBy { it.moduleNumber }
      .flatMap { moduleEntity ->
        moduleEntity.sessionTemplates.sortedBy { it.sessionNumber }
      }

    // If rescheduling, only include sessions after the most recent held session
    if (mostRecentSession != null) {
      allSessionTemplates = allSessionTemplates.filter { template ->
        template.module.moduleNumber > mostRecentSession.moduleNumber ||
          (
            template.module.moduleNumber == mostRecentSession.moduleNumber &&
              template.sessionNumber > mostRecentSession.sessionNumber
            )
      }
    }

    val groupSlots = group.programmeGroupSessionSlots
    require(groupSlots.isNotEmpty()) { "Programme group slots must not be empty" }

    // Priority by (nextDate ASC, startTime ASC)
    val slotQueue = PriorityQueue(
      compareBy<SlotInstance>({ it.nextDate }, { it.slot.startTime }),
    )

    groupSlots.forEach { slot ->
      val firstDate = findNextValidDate(group.earliestPossibleStartDate, slot)
      slotQueue.add(SlotInstance(slot = slot, nextDate = firstDate))
    }

    var firstSessionScheduledAndGapApplied = false

    val generatedSessions = buildList {
      for (template in allSessionTemplates) {
        val nextSlot = slotQueue.poll() ?: break

        val startsAt = LocalDateTime.of(nextSlot.nextDate, nextSlot.slot.startTime)
        val endsAt = startsAt.plusMinutes(template.durationMinutes.toLong())

        add(
          SessionEntity(
            programmeGroup = group,
            moduleSessionTemplate = template,
            startsAt = startsAt,
            endsAt = endsAt,
            locationName = group.deliveryLocationName,
            // If we are scheduling One-to-One here it is a placeholder session
            isPlaceholder = template.sessionType == SessionType.ONE_TO_ONE,
            sessionFacilitators = group.groupFacilitators.map { it.facilitator }.toMutableSet(),
          ),
        )

        if (
          mostRecentSession == null &&
          !firstSessionScheduledAndGapApplied &&
          template.module.moduleNumber == 1 &&
          template.sessionNumber == 1
        ) {
          firstSessionScheduledAndGapApplied = true

          // Regenerate our slot queue plus 3 weeks
          slotQueue.clear()
          groupSlots.forEach { slot ->
            val dateAfterGap = startsAt.toLocalDate().plusWeeks(3)
            val nextValidDate = findNextValidDate(dateAfterGap, slot)
            slotQueue.add(SlotInstance(slot = slot, nextDate = nextValidDate))
          }
        } else {
          val nextWeekDate = nextSlot.nextDate.plusWeeks(1)
          val nextValidDate = findNextValidDate(nextWeekDate, nextSlot.slot)
          slotQueue.add(nextSlot.copy(nextDate = nextValidDate))
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

    // Save the group here so the parent entity is updated and updates the corresponding sessions
    // otherwise JPA does not always update the inverse side of the relationship.
    group.sessions.addAll(generatedSessions)
    programmeGroupRepository.save(group)

    return group.sessions
  }

  fun rescheduleSessionsForGroup(programmeGroupId: UUID): MutableSet<SessionEntity> {
    val group = requireNotNull(programmeGroupRepository.findByIdOrNull(programmeGroupId)) {
      "Group must not be null"
    }
    requireNotNull(group.earliestPossibleStartDate) {
      "Earliest start date must not be null"
    }

    val now = LocalDateTime.now(clock)
    val futureSessions = group.sessions.filter { it.startsAt > now }.toSet()
    val mostRecentSession = group.sessions.filter { it.startsAt <= now }.maxByOrNull { it.startsAt }

    if (futureSessions.isNotEmpty()) {
      // TODO Delete Ndelius appointments here, this will be done as part of https://dsdmoj.atlassian.net/browse/APG-1631
      group.sessions.removeAll(futureSessions)
      programmeGroupRepository.save(group)
    }

    // If there is no prior session, just schedule from start
    return scheduleSessionsForGroup(programmeGroupId, mostRecentSession)
  }

  fun removeFutureSessionsForIndividual(group: ProgrammeGroupEntity, referralId: UUID) {
    log.info("Removing future sessions for referral with id: $referralId from group with id: ${group.id}")
    val now = LocalDateTime.now(clock)
    val futureSessionsToDelete =
      group.sessions.filter { session -> session.startsAt > now && session.attendees.any { it.referral.id == referralId } }
    group.sessions.removeAll(futureSessionsToDelete.toSet())
  }

  fun createNdeliusAppointmentsForSessions(attendees: List<AttendeeEntity>) {
    val (ndeliusAppointments, nDeliusAppointmentEntities) = attendees.map { attendee ->
      // Generate an appointment ID to be used by NDelius
      val appointmentId = UUID.randomUUID()
      val appointment = attendee.toAppointment(appointmentId)
      val appointmentEntity = attendee.toNdeliusAppointmentEntity(appointmentId)
      appointment to appointmentEntity
    }.unzip()

    when (
      val response =
        nDeliusIntegrationApiClient.createAppointmentsInDelius(CreateAppointmentRequest(ndeliusAppointments))
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure to create appointments with reason: ${response.getErrorMessage()}")
        throw BusinessException("Failure to create appointments", response.toException())
      }

      is ClientResult.Failure.Other -> {
        log.warn(
          "Failure to create appointments - Service: ${response.serviceName}, Exception: ${response.exception.message}",
          response.exception,
        )
        throw BusinessException(
          "Failure to create appointments in Ndelius: ${response.exception.message}",
          response.exception,
        )
      }

      is ClientResult.Success -> {
        log.info("${ndeliusAppointments.size} appointments created in Ndelius for group with id: ${nDeliusAppointmentEntities.first().session.programmeGroup.id}")
        nDeliusAppointmentRepository.saveAll(nDeliusAppointmentEntities)
      }
    }
  }

  /**
   * Finds the next valid date (not a bank holiday) starting from the given date,
   * on or after the slot's day of week.
   */
  private fun findNextValidDate(
    fromDate: LocalDate,
    slot: ProgrammeGroupSessionSlotEntity,
  ): LocalDate {
    var candidateDate = fromDate.with(TemporalAdjusters.nextOrSame(slot.dayOfWeek))

    // Keep advancing by weeks until we find a non-holiday
    while (englandAndWalesHolidayDates().contains(candidateDate)) {
      candidateDate = candidateDate.plusWeeks(1)
    }

    return candidateDate
  }

  private data class SlotInstance(
    val slot: ProgrammeGroupSessionSlotEntity,
    val nextDate: LocalDate,
  )

  private fun englandAndWalesHolidayDates(): Set<LocalDate> = when (val response = govUkApiClient.getHolidays()) {
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

  fun removeNDeliusAppointments(nDeliusAppointmentsToRemove: List<NDeliusAppointmentEntity>, sessions: List<SessionEntity>) {
    if (nDeliusAppointmentsToRemove.isEmpty()) return

    when (
      val response = nDeliusIntegrationApiClient.deleteAppointmentsInDelius(
        DeleteAppointmentsRequest(
          appointments = nDeliusAppointmentsToRemove.map { AppointmentReference(it.ndeliusAppointmentId) },
        ),
      )
    ) {
      is ClientResult.Failure.StatusCode -> {
        log.warn("Failure deleting appointments in nDelius with reason: ${response.getErrorMessage()}", response.toException())
        throw BusinessException("Failure deleting appointments in nDelius with status code : ${response.status}", response.toException())
      }

      is ClientResult.Failure.Other -> {
        log.warn("Failure to delete appointments - Service: ${response.serviceName}, Exception: ${response.exception.message}", response.exception)
        throw BusinessException("Failure to delete appointments in NDelius: ${response.exception.message}", response.exception)
      }

      is ClientResult.Success -> {
        sessions.forEach { session ->
          session.ndeliusAppointments
            .removeIf { appointment -> appointment.ndeliusAppointmentId in nDeliusAppointmentsToRemove.map { it.ndeliusAppointmentId } }
        }
        log.info("${nDeliusAppointmentsToRemove.size} appointments deleted in NDelius")
      }
    }
  }
}
