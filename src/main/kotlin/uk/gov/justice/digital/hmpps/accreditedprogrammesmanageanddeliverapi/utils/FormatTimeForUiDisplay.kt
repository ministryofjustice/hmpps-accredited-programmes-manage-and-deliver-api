package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import java.time.LocalTime

fun formatTimeForUiDisplay(time: LocalTime, capitaliseSpecialTimes: Boolean = false): String = when {
  time.hour == 12 && time.minute == 0 -> if (capitaliseSpecialTimes) "Midday" else "midday"
  time.hour == 0 && time.minute == 0 -> if (capitaliseSpecialTimes) "Midnight" else "midnight"
  time.hour == 0 -> "12:${time.minute.toString().padStart(2, '0')}am"
  time.hour < 12 -> if (time.minute == 0) "${time.hour}am" else "${time.hour}:${time.minute.toString().padStart(2, '0')}am"
  time.hour == 12 -> if (time.minute == 0) "12pm" else "12:${time.minute.toString().padStart(2, '0')}pm"
  else -> if (time.minute == 0) "${time.hour - 12}pm" else "${time.hour - 12}:${time.minute.toString().padStart(2, '0')}pm"
}
