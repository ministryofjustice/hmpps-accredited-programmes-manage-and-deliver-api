package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "ndelius_appointment")
class NDeliusAppointmentEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_attendance_id", unique = true)
  var sessionAttendance: SessionAttendanceEntity,

  @Column(name = "licence_condition_id")
  var licenceConditionId: String? = null,

  @Column(name = "requirement_id")
  var requirementId: String? = null,
)
