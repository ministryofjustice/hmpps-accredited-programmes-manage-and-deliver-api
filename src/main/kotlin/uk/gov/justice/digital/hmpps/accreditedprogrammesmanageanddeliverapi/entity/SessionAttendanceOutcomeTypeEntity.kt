package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType.STRING
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity.type.SessionAttendanceOutcomeType

@Entity
@Table(name = "session_attendance_outcome_type")
class SessionAttendanceOutcomeTypeEntity(
  @Id
  @Column(name = "code", nullable = false, length = 5)
  @Enumerated(STRING)
  var code: SessionAttendanceOutcomeType,

  @Column(name = "description")
  var description: String? = null,

  @Column(name = "attendance")
  var attendance: Boolean? = null,

  @Column(name = "compliant", nullable = false)
  var compliant: Boolean,
)
