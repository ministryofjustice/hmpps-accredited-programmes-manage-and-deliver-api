package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.api.model.attendance.SessionAttendee
import uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.common.Constants.UNKNOWN_USER_USERNAME
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "session_attendance")
@EntityListeners(AuditingEntityListener::class)
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
  var outcomeType: SessionAttendanceNDeliusOutcomeEntity,

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.ALL],
    orphanRemoval = true,
    mappedBy = "attendance",
  )
  @OrderBy("createdAt DESC")
  var notesHistory: MutableList<SessionNotesHistoryEntity> = mutableListOf(),

  @NotNull
  @Column(name = "created_by")
  @CreatedBy
  var createdBy: String = SecurityContextHolder.getContext().authentication?.name ?: UNKNOWN_USER_USERNAME,

  @NotNull
  @Column(name = "created_at", updatable = false)
  var createdAt: LocalDateTime = LocalDateTime.now(),
)

/**
 * Selects the latest attendance from an iterable of [SessionAttendanceEntity] by `createdAt`,
 * with a deterministic natural-attribute tiebreak when two rows share the same `createdAt`.
 *
 * Implemented as `maxWithOrNull` over an ascending comparator (max of ASC == latest), so:
 *  1. Row with the highest `createdAt` wins.
 *  2. Ties resolved by alphabetically-highest `createdBy` (non-null String).
 *  3. Any remaining tie resolved by alphabetically-highest `outcomeType.code.name`
 *     (enum name, non-null String).
 *
 * Both tiebreak keys are seed-stable natural attributes. This deliberately avoids the
 * anti-pattern `.thenBy { it.id }` on `@GeneratedValue` UUIDs, which produces different
 * winners between local and CI runs when two rows share the same `createdAt` (a
 * "UUID lottery").
 *
 * Consolidated helper so `SessionService.getRecordAttendanceBySessionId` and
 * `ProgrammeGroupService.getGroupSessionPage` cannot drift apart on the tiebreak choice.
 */
fun Iterable<SessionAttendanceEntity>.latestByCreatedAt(): SessionAttendanceEntity? = maxWithOrNull(
  compareBy<SessionAttendanceEntity> { it.createdAt }
    .thenBy { it.createdBy }
    .thenBy { it.outcomeType.code.name },
)

fun SessionAttendee.toEntity(
  session: SessionEntity,
  groupMembershipEntity: ProgrammeGroupMembershipEntity,
  recordedByFacilitator: FacilitatorEntity,
  outcomeType: SessionAttendanceNDeliusOutcomeEntity,
  createdByFullName: String?,
) = SessionAttendanceEntity(
  session = session,
  groupMembership = groupMembershipEntity,
  recordedByFacilitator = recordedByFacilitator,
  recordedAt = LocalDateTime.now(),
  outcomeType = outcomeType,
).apply {
  sessionNotes?.let {
    notesHistory.add(
      SessionNotesHistoryEntity(
        attendance = this,
        notes = it,
        createdByFullName = createdByFullName,
      ),
    )
  }
}
