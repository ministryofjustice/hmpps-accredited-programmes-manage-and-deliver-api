package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

import java.time.DayOfWeek

data class CreateGroupSessionSlot(val dayOfWeek: DayOfWeek, val hour: Int, val minutes: Int, val amOrPm: AmOrPm)

enum class AmOrPm(val label: String) {
  AM("am"),
  PM("pm"),
}
