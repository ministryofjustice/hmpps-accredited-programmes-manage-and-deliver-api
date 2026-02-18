package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "session_attendance_outcome_type")
class SessionAttendanceOutcomeTypeEntity(
  @Id
  @Column(name = "code", nullable = false, length = 5)
  var code: String,

  @Column(name = "description")
  var description: String? = null,

  @Column(name = "attendance")
  var attendance: Boolean? = null,

  @Column(name = "compliant", nullable = false)
  var compliant: Boolean,
)
