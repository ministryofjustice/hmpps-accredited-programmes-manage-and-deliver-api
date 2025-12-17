package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.SessionRepository
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
  private val sessionRepository: SessionRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleSessionsForGroup(
    programmeGroupId: UUID,
    mostRecentSession: SessionEntity? = null,
  ): MutableList<SessionEntity> {
    val group = requireNotNull(programmeGroupRepository.findByIdOrNull(programmeGroupId)) {
      "Group must not be null"
    }
    val earliestStartDate = requireNotNull(group.earliestPossibleStartDate) {
      "Earliest start date must not be null"
    }
    val templateId = requireNotNull(group.accreditedProgrammeTemplate?.id) {
      "Group template must not be null"
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
      val firstDate = earliestStartDate.with(TemporalAdjusters.nextOrSame(slot.dayOfWeek))
      slotQueue.add(SlotInstance(slot = slot, nextDate = firstDate))
    }

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

        // Advance this slot by one week and re-queue
        slotQueue.add(nextSlot.copy(nextDate = nextSlot.nextDate.plusWeeks(1)))
      }
    }

    log.debug(
      "Generated {} sessions for group code={}, programmeGroupId={}, templateId={}",
      generatedSessions.size,
      group.code,
      programmeGroupId,
      templateId,
    )

    return sessionRepository.saveAll(generatedSessions)
  }

  fun rescheduleSessionsForGroup(programmeGroupId: UUID): MutableList<SessionEntity> {
    val group = requireNotNull(programmeGroupRepository.findByIdOrNull(programmeGroupId)) {
      "Group must not be null"
    }
    requireNotNull(group.earliestPossibleStartDate) {
      "Earliest start date must not be null"
    }

    val now = LocalDateTime.now()
    val futureSessions = group.sessions.filter { it.startsAt > now }.toSet()
    val mostRecentSession = group.sessions.filter { it.startsAt <= now }.maxByOrNull { it.startsAt }

    if (futureSessions.isNotEmpty()) {
      group.sessions.removeAll(futureSessions)
      programmeGroupRepository.save(group)
    }

    // If there is no prior session, just schedule from start
    return scheduleSessionsForGroup(programmeGroupId, mostRecentSession)
  }

  private data class SlotInstance(
    val slot: ProgrammeGroupSessionSlotEntity,
    val nextDate: LocalDate,
  )
}
