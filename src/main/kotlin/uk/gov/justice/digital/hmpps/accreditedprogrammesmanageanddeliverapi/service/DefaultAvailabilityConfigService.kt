package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import java.time.DayOfWeek

@Service
class DefaultAvailabilityConfigService {

  fun getDefaultAvailability(): List<DailyAvailabilityModel> {
    val slotLabels = SlotName.entries.map { it -> it.displayName }
    return DayOfWeek.entries.map { day ->
      DailyAvailabilityModel(
        label = day.toAvailabilityOptions(),
        slots = slotLabels.map { slot ->
          Slot(label = slot, value = false)
        },
      )
    }
  }
}

fun DayOfWeek.toAvailabilityOptions(): AvailabilityOption = when (this) {
  DayOfWeek.MONDAY -> AvailabilityOption.MONDAY
  DayOfWeek.TUESDAY -> AvailabilityOption.TUESDAY
  DayOfWeek.WEDNESDAY -> AvailabilityOption.WEDNESDAY
  DayOfWeek.THURSDAY -> AvailabilityOption.THURSDAY
  DayOfWeek.FRIDAY -> AvailabilityOption.FRIDAY
  DayOfWeek.SATURDAY -> AvailabilityOption.SATURDAY
  DayOfWeek.SUNDAY -> AvailabilityOption.SUNDAY
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
