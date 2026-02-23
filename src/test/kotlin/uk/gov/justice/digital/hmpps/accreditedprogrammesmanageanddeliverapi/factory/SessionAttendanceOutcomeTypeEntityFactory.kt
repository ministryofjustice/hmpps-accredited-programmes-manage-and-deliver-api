package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceOutcomeTypeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceCode.ATTC

class SessionAttendanceOutcomeTypeEntityFactory {
  private var code: SessionAttendanceCode = ATTC
  private var description: String = "Attended - Complied"
  private var attendance: Boolean = true
  private var compliant: Boolean = true

  fun withCode(code: SessionAttendanceCode) = apply { this.code = code }
  fun withDescription(description: String) = apply { this.description = description }
  fun withAttendance(attendance: Boolean) = apply { this.attendance = attendance }
  fun withCompliant(compliant: Boolean) = apply { this.compliant = compliant }

  fun produce() = SessionAttendanceOutcomeTypeEntity(
    code = this.code,
    description = this.description,
    attendance = this.attendance,
    compliant = this.compliant,
  )
}
