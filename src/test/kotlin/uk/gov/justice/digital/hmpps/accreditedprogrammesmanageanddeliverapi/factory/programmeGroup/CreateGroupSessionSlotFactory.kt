package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import java.time.DayOfWeek
import kotlin.random.Random

class CreateGroupSessionSlotFactory {
  private var dayOfWeek: DayOfWeek = DayOfWeek.MONDAY
  private var hour: Int = 12
  private var minute: Int = 0
  private var amOrPm: AmOrPm = AmOrPm.AM

  fun withDayOfWeek(day: DayOfWeek) = apply { this.dayOfWeek = day }

  fun withHour(hour: Int): CreateGroupSessionSlotFactory {
    require(hour in 1..12) { "Hour must be in range 0..12" }
    return apply { this.hour = hour }
  }

  fun withMinute(minute: Int): CreateGroupSessionSlotFactory {
    require(minute in 0..59) { "Hour must be in 0..23" }
    return apply { this.minute = minute }
  }

  fun withAmOrPm(amOrPm: AmOrPm) = apply { this.amOrPm = amOrPm }

  fun produce(): CreateGroupSessionSlot = CreateGroupSessionSlot(
    dayOfWeek = this.dayOfWeek,
    hour = this.hour,
    minutes = this.minute,
    amOrPm = this.amOrPm,
  )
}
