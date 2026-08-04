package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import java.time.LocalTime

fun formatTimeOfSession(startTime: LocalTime, endTime: LocalTime, capitaliseMidday: Boolean = false): String {
  fun formatTime(time: LocalTime): String = when {
    time.hour == 12 && time.minute == 0 -> if (capitaliseMidday) "Midday" else "midday"
    time.hour == 0 && time.minute == 0 -> if (capitaliseMidday) "Midnight" else "midnight"
    time.hour == 0 -> "12:${time.minute.toString().padStart(2, '0')}am"
    time.hour < 12 -> if (time.minute == 0) "${time.hour}am" else "${time.hour}:${time.minute.toString().padStart(2, '0')}am"
    time.hour == 12 -> if (time.minute == 0) "12pm" else "12:${time.minute.toString().padStart(2, '0')}pm"
    else -> if (time.minute == 0) "${time.hour - 12}pm" else "${time.hour - 12}:${time.minute.toString().padStart(2, '0')}pm"
  }
  return "${formatTime(startTime)} to ${formatTime(endTime)}"
}
