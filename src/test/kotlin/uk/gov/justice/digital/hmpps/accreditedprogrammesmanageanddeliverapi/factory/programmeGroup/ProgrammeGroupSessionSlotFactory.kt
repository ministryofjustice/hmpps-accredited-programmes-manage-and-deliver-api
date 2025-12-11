package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.ProgrammeGroupSessionSlotEntity
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

class ProgrammeGroupSessionSlotFactory {

  fun produce(
    id: UUID? = null,
    programmeGroup: ProgrammeGroupEntity? = null,
    dayOfWeek: DayOfWeek? = null,
    startTime: LocalTime? = null,
  ): ProgrammeGroupSessionSlotEntity = ProgrammeGroupSessionSlotEntity(
    id = id,
    programmeGroup = programmeGroup ?: ProgrammeGroupFactory().produce(),
    dayOfWeek = dayOfWeek ?: DayOfWeek.entries.random(),
    startTime = startTime ?: LocalTime.now(),
  )
}
