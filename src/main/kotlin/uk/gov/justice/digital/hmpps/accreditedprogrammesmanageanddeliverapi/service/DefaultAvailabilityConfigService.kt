package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SlotName
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.AvailabilityOption
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.DailyAvailabilityModel
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.model.Slot
import java.time.DayOfWeek

/**
 *
 * This is a config class, when there is a requirement to display Weekdays or Weekends,
 * the config can be moved into the database
 *
 */

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
