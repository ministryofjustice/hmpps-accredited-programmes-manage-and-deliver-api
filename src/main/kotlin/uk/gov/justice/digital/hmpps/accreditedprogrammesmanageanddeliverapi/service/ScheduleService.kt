package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ModuleRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupRepository
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.repository.ProgrammeGroupSessionSlotRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.PriorityQueue
import java.util.UUID

data class SlotInstance(
  val slot: ProgrammeGroupSessionSlotEntity,
  var nextDate: LocalDate, // actual scheduled date, moves forward weekly after use
) : Comparable<SlotInstance> {

  override fun compareTo(other: SlotInstance): Int = compareValuesBy(this, other, { it.nextDate }, { it.slot.startTime })
}

@Service
class ScheduleService(
  val programmeGroupRepository: ProgrammeGroupRepository,
  val programmeGroupSessionSlotRepository: ProgrammeGroupSessionSlotRepository,
  val programmeGroupModuleRepository: ModuleRepository,
) {

  fun scheduleSessionForGroup(programmeGroupId: UUID): MutableList<SessionEntity> {
    val group = programmeGroupRepository.findByIdOrNull(programmeGroupId)
    require(group != null) { "Group must not be null" }
    require(group.earliestPossibleStartDate != null) { "Earliest start date must not be null" }
    val templateId = group.accreditedProgrammeTemplate?.id
    require(templateId != null) { "Group template must not be null" }
    val allSessionTemplates =
      programmeGroupModuleRepository.findByAccreditedProgrammeTemplateId(templateId).sortedBy { it.moduleNumber }
        .flatMap { moduleEntity -> moduleEntity.sessionTemplates.sortedBy { it.sessionNumber } }

    val groupSlots = group.programmeGroupSessionSlots
    require(groupSlots.isNotEmpty()) { "Programme group slots must not be empty or null" }
    val queue = buildSlotQueue(groupSlots, group.earliestPossibleStartDate!!)

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
        isCatchup = false,
      )

      generatedSessions.add(session)

      nextSlot.nextDate = nextSlot.nextDate.plusWeeks(1)
      queue.add(nextSlot)
    }

    return generatedSessions
  }

  private fun buildSlotQueue(
    slots: Set<ProgrammeGroupSessionSlotEntity>,
    startDate: LocalDate,
  ): PriorityQueue<SlotInstance> {
    val queue = PriorityQueue<SlotInstance>()

    slots.forEach { slot ->
      val firstDate = startDate.with(TemporalAdjusters.nextOrSame(slot.dayOfWeek))
      queue.add(SlotInstance(slot, firstDate))
    }
    return queue
  }
}
