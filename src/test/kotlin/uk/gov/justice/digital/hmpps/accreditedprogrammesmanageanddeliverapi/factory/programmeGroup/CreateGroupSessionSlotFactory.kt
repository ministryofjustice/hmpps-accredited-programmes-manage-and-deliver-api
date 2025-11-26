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
}
