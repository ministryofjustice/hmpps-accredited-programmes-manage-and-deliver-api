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
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionRequest
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.ScheduleSessionResponse
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.SessionTime
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.FacilitatorRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ModuleSessionTemplateRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupMembershipRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
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
  private val sessionRepository: SessionRepository,
  private val clock: Clock,
  private val programmeGroupMembershipRepository: ProgrammeGroupMembershipRepository,
  private val moduleSessionTemplateRepository: ModuleSessionTemplateRepository,
  private val facilitatorRepository: FacilitatorRepository,
  private val govUkApiClient: GovUkApiClient,
  private val facilitatorRepository: FacilitatorRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleIndividualSession(groupId: UUID, request: ScheduleSessionRequest): ScheduleSessionResponse {
    programmeGroupRepository.findByIdOrNull(groupId)
      ?: throw NotFoundException("Group with id: $groupId could not be found")

    val moduleSessionTemplate = moduleSessionTemplateRepository.findByIdOrNull(request.sessionTemplateId)
      ?: throw NotFoundException("Session template with id: ${request.sessionTemplateId} could not be found")

    val facilitatorEntity = facilitatorRepository.findByNdeliusPersonCode(request.facilitators.first().facilitatorCode)
      ?: throw NotFoundException("Facilitator with code ${request.facilitators.first().facilitatorCode} could not be found")

    val session = sessionRepository.findByModuleSessionTemplateIdAndProgrammeGroupId(moduleSessionTemplate.id!!, groupId).first()
    session.startsAt = convertToLocalDateTime(request.startDate, request.startTime)
    session.endsAt = convertToLocalDateTime(request.startDate, request.endTime)

    request.referralIds.forEach { referralId ->
      val membership = programmeGroupMembershipRepository.findNonDeletedByReferralAndGroupIds(referralId, groupId)
        ?: throw NotFoundException("Active membership for referral $referralId in group $groupId not found")

      session.attendances.add(
        SessionAttendanceEntity(
          session = session,
          groupMembership = membership,
          recordedByFacilitator = facilitatorEntity,
        ),
      )
    }
    sessionRepository.save(session)
    return ScheduleSessionResponse(message = "Session scheduled successfully")
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

  fun scheduleSessionsForGroup(
    programmeGroupId: UUID,
    mostRecentSession: SessionEntity? = null,
  ): MutableList<SessionEntity> {
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
          session.attendances.add(
            SessionAttendanceEntity(
              session = session,
              groupMembership = groupMembership,
            ),
          )
        }
      }
    }

    return sessionRepository.saveAll(generatedSessions)
  }

  fun rescheduleSessionsForGroup(programmeGroupId: UUID): MutableList<SessionEntity> {
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
      group.sessions.removeAll(futureSessions)
      programmeGroupRepository.save(group)
    }

    // If there is no prior session, just schedule from start
    return scheduleSessionsForGroup(programmeGroupId, mostRecentSession)
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

    is ClientResult.Success ->
      response.body.englandAndWales.events
        .mapNotNull { event ->
          runCatching { LocalDate.parse(event.date) }.getOrNull()
        }
        .toSet()
  }
}
