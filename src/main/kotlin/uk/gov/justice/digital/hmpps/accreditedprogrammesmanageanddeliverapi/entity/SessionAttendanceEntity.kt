package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
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

  @Column(name = "legitimate_absence")
  var legitimateAbsence: Boolean? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by_facilitator_id")
  var recordedByFacilitator: FacilitatorEntity? = null,

  @Column(name = "recorded_at")
  var recordedAt: LocalDateTime? = null,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "outcome_type_code")
  var outcomeType: SessionAttendanceOutcomeTypeEntity,

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    mappedBy = "attendance",
  )
  @OrderBy("createdAt DESC")
  var notesHistory: MutableList<SessionNotesHistoryEntity> = mutableListOf(),
)

fun SessionAttendee.toEntity(
  session: SessionEntity,
  groupMembershipEntity: ProgrammeGroupMembershipEntity,
  recordedByFacilitator: FacilitatorEntity,
  outcomeType: SessionAttendanceOutcomeTypeEntity,
) = SessionAttendanceEntity(
  session = session,
  groupMembership = groupMembershipEntity,
  recordedByFacilitator = recordedByFacilitator,
  recordedAt = LocalDateTime.now(),
  outcomeType = outcomeType,
).apply {
  sessionNotes?.let {
    notesHistory.add(SessionNotesHistoryEntity(attendance = this, notes = it))
  }
}
