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
  private val module: ModuleRepository,
  private val sessionRepository: SessionRepository,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun scheduleSessionsForGroup(programmeGroupId: UUID, mostRecentSession: SessionEntity? = null): MutableList<SessionEntity> {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
    require(group != null) { "Group must not be null" }
    require(group.earliestPossibleStartDate != null) { "Earliest start date must not be null" }

    val templateId = group.accreditedProgrammeTemplate?.id
    require(templateId != null) { "Group template must not be null" }

    var allSessionTemplates =
      module.findByAccreditedProgrammeTemplateId(templateId)
        .sortedBy { it.moduleNumber }
        .flatMap { moduleEntity -> moduleEntity.sessionTemplates.sortedBy { it.sessionNumber } }

    // In the case of a reschedule, filter out sessions in the past
    if (mostRecentSession != null) {
      allSessionTemplates = allSessionTemplates.filter { it.module.moduleNumber >= mostRecentSession.moduleNumber }.toMutableList()
      val templatesToRemove = allSessionTemplates.filter { it.module.moduleNumber == mostRecentSession.moduleNumber && it.sessionNumber <= mostRecentSession.sessionNumber }.toMutableList()
      allSessionTemplates.removeAll(templatesToRemove)
      // .filter { it.module.moduleNumber == mostRecentSession.moduleNumber }.filter { it.sessionNumber >= mostRecentSession.sessionNumber }
    }

    val groupSlots = group.programmeGroupSessionSlots
    require(groupSlots.isNotEmpty()) { "Programme group slots must not be empty or null" }
    val queue = PriorityQueue<SlotInstance>()

    groupSlots.forEach { slot ->
      val firstDate = group.earliestPossibleStartDate!!.with(TemporalAdjusters.nextOrSame(slot.dayOfWeek))
      queue.add(SlotInstance(slot, firstDate))
    }

    val generatedSessions = mutableListOf<SessionEntity>()

    for (sessionTemplate in allSessionTemplates) {
      val nextSlot = queue.poll() ?: break

      val startDateTime = LocalDateTime.of(nextSlot.nextDate, nextSlot.slot.startTime)
      val endDateTime = startDateTime.plusMinutes(sessionTemplate.durationMinutes.toLong())

      val session = SessionEntity(
        programmeGroup = group,
        moduleSessionTemplate = sessionTemplate,
        startsAt = startDateTime,
        endsAt = endDateTime,
        locationName = group.deliveryLocationName,
      )

      generatedSessions.add(session)

      nextSlot.nextDate = nextSlot.nextDate.plusWeeks(1)
      queue.add(nextSlot)
    }
    log.debug("Generated ${generatedSessions.size} sessions for group: ${group.code}")
    return sessionRepository.saveAll(generatedSessions)
  }

  fun rescheduleSessionsForGroup(programmeGroupId: UUID): MutableList<SessionEntity> {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
    require(group != null) { "Group must not be null" }
    require(group.earliestPossibleStartDate != null) { "Earliest start dat e must not be null" }

    // Check there are no existing sessions for the group, if there are, delete them (reschedule)
    group.sessions.removeAll { it.startsAt > LocalDateTime.now() }
    programmeGroupRepository.save(group)

    val mostRecentSession = group.sessions.filter { it.startsAt <= LocalDateTime.now() }.maxByOrNull { it.startsAt }!!

    return scheduleSessionsForGroup(programmeGroupId, mostRecentSession)
  }

  private data class SlotInstance(
    val slot: ProgrammeGroupSessionSlotEntity,
    var nextDate: LocalDate,
  ) : Comparable<SlotInstance> {

    override fun compareTo(other: SlotInstance): Int = compareValuesBy(this, other, { it.nextDate }, { it.slot.startTime })
  }
}
