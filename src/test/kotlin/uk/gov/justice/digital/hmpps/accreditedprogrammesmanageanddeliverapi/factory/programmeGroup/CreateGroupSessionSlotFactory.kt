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
  ): CreateGroupSessionSlot = CreateGroupSessionSlot(
    dayOfWeek = dayOfWeek ?: randomDayOfWeek(),
    hour = hour ?: Random.nextInt(1, 13), // 1–12 inclusive
    minutes = minutes ?: Random.nextInt(0, 60), // 0–59 inclusive
    amOrPm = amOrPm ?: randomAmOrPm(),
  )

  private fun randomDayOfWeek(): DayOfWeek = DayOfWeek.entries.random()

  private fun randomAmOrPm(): AmOrPm = AmOrPm.entries.random()

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
