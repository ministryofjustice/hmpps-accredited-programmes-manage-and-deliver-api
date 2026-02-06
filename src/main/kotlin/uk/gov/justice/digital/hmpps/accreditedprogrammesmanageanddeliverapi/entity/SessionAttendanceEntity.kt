package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "session_attendance")
class SessionAttendanceEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id")
  var session: SessionEntity,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_membership_id")
  var groupMembership: ProgrammeGroupMembershipEntity,

  @Column(name = "attended")
  var attended: Boolean? = null,

  @Column(name = "did_not_engage")
  var didNotEngage: Boolean? = null,

  @Column(name = "legitimate_absence")
  var legitimateAbsence: Boolean? = null,

  @Column(name = "notes")
  var notes: String? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by_facilitator_id")
  var recordedByFacilitator: FacilitatorEntity? = null,

  @Column(name = "recorded_at")
  var recordedAt: LocalDateTime? = null,
)
