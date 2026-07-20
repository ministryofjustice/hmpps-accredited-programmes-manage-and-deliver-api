package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory.programmeGroup

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.AmOrPm
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup.CreateGroupSessionSlot
import java.time.DayOfWeek
import kotlin.random.Random

class CreateGroupSessionSlotFactory {

  fun produce(
    dayOfWeek: DayOfWeek? = null,
    hour: Int? = null,
    minutes: Int? = null,
    amOrPm: AmOrPm? = null,
  ): CreateGroupSessionSlot {
    // Constrain session times to not overlap midnight
    val hour24 = Random.nextInt(6, 21)
    return CreateGroupSessionSlot(
      dayOfWeek = dayOfWeek ?: randomDayOfWeek(),
      hour = hour ?: if (hour24 % 12 == 0) 12 else hour24 % 12,
      minutes = minutes ?: Random.nextInt(0, 60), // 0–59 inclusive
      amOrPm = amOrPm ?: if (hour24 < 12) AmOrPm.AM else AmOrPm.PM,
    )
  }

  private fun randomDayOfWeek(): DayOfWeek = DayOfWeek.entries.random()

  fun produceUniqueSlots(
    count: Int,
    overrideHour: Int? = null,
    overrideMinutes: Int? = null,
    overrideAmOrPm: AmOrPm? = null,
  ): Set<CreateGroupSessionSlot> {
    require(count in 1..7) {
      "count must be between 1 and 7 because there are only 7 unique days."
    }

    val selectedDays = DayOfWeek.entries.shuffled().take(count)

    return selectedDays.map { day ->
      produce(
        dayOfWeek = day,
        hour = overrideHour,
        minutes = overrideMinutes,
        amOrPm = overrideAmOrPm,
      )
    }.toSet()
  }
}
