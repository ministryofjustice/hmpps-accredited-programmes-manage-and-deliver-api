package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.utils

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.UAAB

class AttendanceUtils {
  companion object {
    fun getAttendanceTextFromOutcome(
      attendanceOutcomeCode: SessionAttendanceNDeliusCode?,
      attendanceOutcomeDescription: String?,
    ): String = when (attendanceOutcomeCode) {
      UAAB -> "Did not attend"
      null -> "To be confirmed"
      else -> attendanceOutcomeDescription!!
    }
  }
}
