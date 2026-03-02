package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.programmeGroup

enum class SessionScheduleType {
  SCHEDULED,
  CATCH_UP,
  ;

  val isCatchUp: Boolean get() = this == CATCH_UP
}
