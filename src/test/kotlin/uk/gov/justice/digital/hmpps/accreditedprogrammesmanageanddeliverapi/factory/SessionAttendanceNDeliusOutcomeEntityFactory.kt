package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.factory

import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.SessionAttendanceNDeliusOutcomeEntity
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceNDeliusCode.ATTC

class SessionAttendanceNDeliusOutcomeEntityFactory {
  private var code: SessionAttendanceNDeliusCode = ATTC
  private var description: String = "Attended - Complied"
  private var attendance: Boolean = true
  private var compliant: Boolean = true

  fun withCode(code: SessionAttendanceNDeliusCode) = apply { this.code = code }
  fun withDescription(description: String) = apply { this.description = description }
  fun withAttendance(attendance: Boolean) = apply { this.attendance = attendance }
  fun withCompliant(compliant: Boolean) = apply { this.compliant = compliant }

  fun produce() = SessionAttendanceNDeliusOutcomeEntity(
    code = this.code,
    description = this.description,
    attendance = this.attendance,
    compliant = this.compliant,
  )
}
