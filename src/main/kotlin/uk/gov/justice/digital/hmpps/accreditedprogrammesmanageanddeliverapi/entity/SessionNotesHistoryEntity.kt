package uk.gov.justice.digital.hmpps.accreditedprogrammesmanageanddeliverapi.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.security.core.context.SecurityContextHolder
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "session_notes_history")
@EntityListeners(AuditingEntityListener::class)
class SessionNotesHistoryEntity(
  @Id
  @GeneratedValue
  @Column(name = "id")
  var id: UUID? = null,

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "attendance_id")
  var attendance: SessionAttendanceEntity,

  @Column(name = "notes")
  var notes: String? = null,

  @NotNull
  @Column(name = "created_by")
  @CreatedBy
  var createdBy: String? = SecurityContextHolder.getContext().authentication?.name ?: "UNKNOWN_USER",

  @NotNull
  @CreatedDate
  @Column(name = "created_at")
  var createdAt: LocalDateTime? = LocalDateTime.now(),
)
