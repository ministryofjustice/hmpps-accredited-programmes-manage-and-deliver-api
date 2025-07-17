package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import java.time.DayOfWeek

@Service
class DefaultAvailabilityConfigService {

  fun getDefaultAvailability(): List<DailyAvailabilityModel> {
    val slotLabels = SlotName.entries.map { it -> it.displayName }
    return DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toPluralLabel(),
        slots = slotLabels.map { slot ->
          Slot(label = slot, value = false)
        },
      )
    }
  }
}

fun DayOfWeek.toPluralLabel(): String = when (this) {
  DayOfWeek.MONDAY -> "Mondays"
  DayOfWeek.TUESDAY -> "Tuesdays"
  DayOfWeek.WEDNESDAY -> "Wednesdays"
  DayOfWeek.THURSDAY -> "Thursdays"
  DayOfWeek.FRIDAY -> "Fridays"
  DayOfWeek.SATURDAY -> "Saturdays"
  DayOfWeek.SUNDAY -> "Sundays"
}

fun String.toDayOfWeek(): DayOfWeek = when (this) {
  "Mondays" -> DayOfWeek.MONDAY
  "Tuesdays" -> DayOfWeek.TUESDAY
  "Wednesdays" -> DayOfWeek.WEDNESDAY
  "Thursdays" -> DayOfWeek.THURSDAY
  "Fridays" -> DayOfWeek.FRIDAY
  "Saturdays" -> DayOfWeek.SATURDAY
  "Sundays" -> DayOfWeek.SUNDAY
  else -> {
    throw IllegalArgumentException("Invalid DayOfWeek: $this")
  }
}
